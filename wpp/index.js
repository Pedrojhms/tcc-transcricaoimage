const { Client, LocalAuth, MessageMedia } = require('whatsapp-web.js');
const qrcode = require('qrcode-terminal');
const QRCode = require('qrcode');
const path = require('path');
const axios = require('axios');
const express = require('express');
const bodyParser = require('body-parser');
const puppeteer = require('puppeteer');

const QR_SAVE_PATH = path.resolve(__dirname, 'qrcode.png');
const app = express();

const URL_JAVA_APP = "http://app:8080/api";

app.use(bodyParser.json({limit: '50mb'}));

// Estado por usuário
// userState[userId] = { sentImage: bool, audioSent: bool, imageId: string, questionNumber: int }
const userState = {};

// Função para inicializar o Puppeteer
async function initializePuppeteer() {
    try {
        const browser = await puppeteer.launch({
            headless: true,
            executablePath: '/usr/bin/chromium-browser',
            args: [
                '--no-sandbox',
                '--disable-setuid-sandbox',
                '--disable-dev-shm-usage',
                '--disable-accelerated-2d-canvas',
                '--no-first-run',
                '--no-zygote',
                '--disable-gpu',
                '--disable-background-timer-throttling',
                '--disable-backgrounding-occluded-windows',
                '--disable-renderer-backgrounding'
            ]
        });
        console.log('Puppeteer inicializado com sucesso');
        return browser;
    } catch (error) {
        console.error('Erro ao inicializar Puppeteer:', error);
        return null;
    }
}

// Configuração do cliente WhatsApp
const client = new Client({
    authStrategy: new LocalAuth(),
    puppeteer: {
        executablePath: process.env.PUPPETEER_EXECUTABLE_PATH || '/usr/bin/chromium-browser',
        args: ['--no-sandbox', '--disable-setuid-sandbox']
    }
});

// QR code
client.on('qr', (qr) => {
    console.log('QR code recebido, escaneie com o app do WhatsApp:');
    qrcode.generate(qr, {small: true});
    QRCode.toFile(QR_SAVE_PATH, qr, function (err) {
        if (err) {
            console.error('Erro ao salvar QR:', err);
        } else {
            console.log(`QR code salvo em: ${QR_SAVE_PATH}`);
        }
    });
});

let whatsappReady = false;

client.on('ready', () => {
    whatsappReady = true;
    console.log('Cliente está pronto!');
});

// RECEBE MENSAGENS
client.on('message', async (message) => {
    const userId = message.from;
    // Inicializa estado do usuário
    userState[userId] = userState[userId] || { sentImage: false, audioSent: false, imageId: null, questionNumber: 1 };

    // Se veio texto antes de imagem
    if (message.type === 'chat' && !message.hasMedia && !userState[userId].sentImage) {
        await client.sendMessage(userId,
            'Por favor, envie uma imagem pela galeria ou câmera do seu celular para gerar a descrição antes de enviar mensagens de texto.');
        return;
    }

    // Se veio texto, mas o áudio ainda não foi enviado (aguardando processamento)
    if (message.type === 'chat' && userState[userId].sentImage && !userState[userId].audioSent) {
        await client.sendMessage(userId,
            'Aguarde o envio do áudio com a descrição antes de responder ao questionário.');
        return;
    }

    // Se veio texto, áudio já foi enviado: resposta do questionário
    if (message.type === 'chat' && userState[userId].sentImage && userState[userId].audioSent) {
        // Envia a resposta para o backend, recebe próxima pergunta ou mensagem final
        try {
            const score = message.body.trim();
            if (!['1','2','3','4','5'].includes(score)) {
                await client.sendMessage(userId, 'Por favor, responda apenas com um número de 1 a 5.');
                return;
            }

            // Recupera dados do estado do usuário
            const imageId = userState[userId].imageId;

            const payload = {
                from: userId,
                imageId: imageId,
                questionNumber: userState[userId].questionNumber,
                score: parseInt(score)
            };

            // Envia para o backend
            const response = await axios.post(URL_JAVA_APP + '/whatsapp-survey', payload);

            // Incrementa o número da pergunta para o próximo envio
            userState[userId].questionNumber++;

            // Envia próxima pergunta ou mensagem final
            const nextMessage = response.data.message || '✅ Obrigado!';
            await client.sendMessage(userId, nextMessage);

            // Se o backend sinalizar fim do questionário, limpa estado
            if (response.data.finished) {
                userState[userId].sentImage = false;
                userState[userId].audioSent = false;
                userState[userId].imageId = null;
                userState[userId].questionNumber = 1;
            }
        } catch (err) {
            console.error('Erro ao enviar resposta do questionário:', err);
            await client.sendMessage(userId, 'Ocorreu um erro ao enviar sua resposta, tente novamente.');
        }
        return;
    }

    // Se veio imagem/mídia
    if (message.hasMedia) {
        userState[userId].sentImage = true;
        userState[userId].audioSent = false;
        userState[userId].imageId = null;
        userState[userId].questionNumber = 1; // Reinicia questionário ao receber nova imagem

        const media = await message.downloadMedia();
        const payload = {
            id: message.id._serialized,
            from: message.from,
            to: message.to,
            body: message.body,
            type: message.type,
            timestamp: message.timestamp,
            author: message.author,
            isForwarded: message.isForwarded,
            hasMedia: message.hasMedia,
            media: {
                mimetype: media.mimetype,
                data: media.data,
                filename: media.filename || null
            }
        };
        await axios.post(URL_JAVA_APP + '/whatsapp-webhook', payload);
    }
});

// --------- ENDPOINTS HTTP ---------

// Enviar mensagem de texto
app.post('/sendText', async (req, res) => {
    const { to, message, imageId } = req.body;
    if (!to || !message) {
        return res.status(400).json({ error: 'Parâmetros "to" e "message" são obrigatórios.' });
    }
    try {
        await client.sendMessage(to, message);
        // Se receber imageId, salva para uso nas respostas e inicia questionário
        if (imageId) {
            userState[to] = userState[to] || {};
            userState[to].imageId = imageId;
            userState[to].questionNumber = 1;
        }
        res.status(200).json({ status: 'OK', to, message });
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});

// Enviar mensagem de voz (áudio .ogg base64)
app.post('/sendVoice', async (req, res) => {
    if (!whatsappReady) {
        return res.status(503).json({ error: 'WhatsApp não está pronto. Aguarde.' });
    }

    const { to, audioBase64, imageId } = req.body;
    if (!to || !audioBase64) {
        return res.status(400).json({ error: 'Parâmetros "to" e "audioBase64" são obrigatórios.' });
    }
    try {
        const media = new MessageMedia('audio/ogg; codecs=opus', audioBase64, 'audio.ogg');
        await client.sendMessage(to, media, { sendAudioAsVoice: true });
        // Marca que o áudio foi enviado, libera questionário
        userState[to] = userState[to] || {};
        userState[to].audioSent = true;
        // Se receber imageId, salva para uso nas respostas
        if (imageId) {
            userState[to].imageId = imageId;
        }
        res.status(200).json({ status: 'OK', to });
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});

app.get('/health', (req, res) => {
    res.status(200).json({
        status: 'healthy',
        timestamp: new Date().toISOString(),
        uptime: process.uptime()
    });
});

// Função principal para inicializar tudo
async function main() {
    try {
        // Inicializa Puppeteer (opcional, só se precisar)
        await initializePuppeteer();

        // Inicializa cliente WhatsApp
        client.initialize();

        // Inicia servidor HTTP
        const PORT = 3000;
        app.listen(PORT, () => {
            console.log(`Servidor HTTP escutando na porta ${PORT}`);
        });

    } catch (error) {
        console.error('Erro ao inicializar aplicação:', error);
        process.exit(1);
    }
}

// Chama a função principal
main();