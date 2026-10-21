# TTS plugin
> Plugins are written in JavaScript and use Rhino as the JS engine.

```javascript
let PluginJS = {
    "name": "Plugin name",
    "id": "unique.plugin.id", // The plugin id is the relative directory
    "author": "Author",
    "iconUrl": "https://cn.bing.com/favicon.ico",
    "version": 1, // Version number; must be an integer

    // Called when TTS stops
    "onStop": function () {
 
    },
    
    /**
    "getAudio": function (text, locale, voice, speed, volume, pitch) {
        // Supported return types:
        // A string starting with http:// or https://
        // InputStream, a Java input stream
        // ByteArray, a Java byte array
        // ArrayBuffer
        // Uint8Array
        
    }
    */

    // Use either this or getAudio, as needed
    "getAudioV2": function (request, callback) {
        let rate = (request.rate * 2) - 100
        let pitch = request.pitch - 50
        let voice = request.voice
        let volume = request.volume
        let text = request.text

        callback.write(bytes)   // Write a byte array
        callback.close()        // Call after all bytes have been written
        callback.error(string)  // Call when an error occurs
    },
}


let EditorJS = {
    // Audio sample rate. Called when saving a TTS config.
    "getAudioSampleRate": function (locale, voice) {
        // return 24000
        
        // Automatically request a short audio clip and detect the rate
        let audio = PluginJS.getAudio('test', locale, voice, 50, 50, 50)
        return ttsrv.getAudioSampleRate(audio)
    },

    // Locale dropdown
    "getLocales": function () {
        return ['zh-CN', 'en-US']
    },

    // Voice dropdown
    "getVoices": function (locale) {
        // Simple usage: key is the voice argument for getAudio, value is the display name
        return { 'xiaoxiao': 'Xiaoxiao' }
        
        // Advanced usage: specify an icon
        return { 
            'xiaoxiao': {
                name: 'Xiaoxiao',
                icon: 'male' // Optional values: male / female / icon URL
            }
        }
      
    },

    // Load voice data
    "onLoadData": function () {
        let jsonStr = ''
        if (fs.exists('voices.json')) {
            jsonStr = fs.readText('voices.json')
        } else {
            let url = 'https://speech.platform.bing.com/consumer/speech/synthesize/readaloud/voices/list?trustedclienttoken=' + token
            jsonStr = http.get(url).text()
            fs.writeFile('voices.json', jsonStr)
        }

        voices = JSON.parse(jsonStr)
    },

    "onLoadUI": function (ctx, linerLayout) {

    },

    "onVoiceChanged": function (locale, voiceCode) {

    }
}
```
