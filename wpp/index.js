const { Client, LocalAuth, MessageMedia } = require('whatsapp-web.js');
const qrcode = require('qrcode-terminal');
const QRCode = require('qrcode');
const path = require('path');
const axios = require('axios');
const express = require('express');
const bodyParser = require('body-parser');

const QR_SAVE_PATH = path.resolve(__dirname, 'qrcode.png');
const app = express();
app.use(bodyParser.json({limit: '50mb'}));

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

// Recebe mensagens e encaminha para webhook
client.on('message', async (message) => {
    try {
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
        };

        if (message.hasMedia) {
            const media = await message.downloadMedia();
            payload.media = {
                mimetype: media.mimetype,
                data: media.data, // base64 string
                filename: media.filename || null
            };
        }

        await axios.post('http://app:8080/api/whatsapp-webhook', payload);
        console.log('Mensagem enviada para o webhook:', payload);
    } catch (err) {
        console.error('Erro ao enviar mensagem para o webhook:', err.message);
    }
});

client.initialize();

// --------- ENDPOINTS HTTP ---------

// Enviar mensagem de texto
app.post('/sendText', async (req, res) => {
    const { to, message } = req.body;
    if (!to || !message) {
        return res.status(400).json({ error: 'Parâmetros "to" e "message" são obrigatórios.' });
    }
    try {
        await client.sendMessage(to, message);
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

    const { to, audioBase64 } = req.body;
    if (!to || !audioBase64) {
        return res.status(400).json({ error: 'Parâmetros "to" e "audioBase64" são obrigatórios.' });
    }
    try {
        // O mimetype do WhatsApp para áudio (voice message) é 'audio/ogg; codecs=opus'
        const media = new MessageMedia('audio/ogg; codecs=opus', audioBase64, 'audio.ogg');
        await client.sendMessage(to, media, { sendAudioAsVoice: true });
        res.status(200).json({ status: 'OK', to });
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});

// Inicia servidor HTTP (porta 3000)
const PORT = 3000;
app.listen(PORT, () => {
    console.log(`Servidor HTTP escutando na porta ${PORT}`);
});