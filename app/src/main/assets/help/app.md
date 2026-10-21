version-2

[![QQ group](https://img.shields.io/badge/QQ-124841768-blue.svg)](https://jq.qq.com/?_wv=1027&k=y7WCDjEA)
[![Issue](https://img.shields.io/badge/Github-Issue-greeb.svg)](https://github.com/jing332/tts-server-android/issues)
[![Dev](https://img.shields.io/github/actions/workflow/status/jing332/tts-server-android/test.yml?label=Dev)](https://github.com/jing332/tts-server-android/actions/workflows/test.yml)

# TTS Server
This app does not host text-to-speech. It forwards network TTS through plugins that call TTS APIs on the internet.


## 1. System TTS
The four screens below each have their own import and export actions in the More options menu at the top right. You can also back up and restore everything in Settings.

### Main list
A list of TTS configs. You can use groups to switch several configs at once.
- In Settings you can swap the `Edit` and `Preview` buttons. ( <i>Long-press Edit to preview, and long-press Preview to edit.</i> )

### Speech rules
Speech rules process the text to read and match a TTS config by the tags you set (for example, narration and dialogue).
The app includes a built-in `Narration/Dialogue` speech rule based on Chinese quotation marks. You can use it as-is.

### Plugins
Plugins extend TTS. A JavaScript plugin calls a TTS API on the internet, for example the built-in `Azure` plugin.

### Replace rules
Replace rules rewrite the text before it is read, for example to correct pronunciation: replace "hello" with "hi".

Advanced example:
- Replace the quotation marks around dialogue of 5 characters or fewer with brackets, so it is read as narration.
```
(Enable regular expressions)
Replace rule: (")(.{1,5})(")
Replace with: [$2]
```

## System TTS FAQ
### 1. Playback stops a while after the screen turns off?
> Add this app and your reader app to the battery optimization whitelist in `System settings -> Apps -> Battery optimization`.
>
> For this app, open the navigation drawer and tap `Battery optimization whitelist`.
>
> PS: On some phone systems you may also need to lock the background task and allow background activity.

### 2. Long gaps between paragraphs?
> This is usually network delay. Android system TTS cannot precache audio, so each request has to be fetched synchronously.



## 2. TTS forwarder
Exposes Android system TTS as an HTTP API so a reader app can call it.
**Used with a reader app's network TTS engine, it can precache a chapter of audio and smooth out the gaps between paragraphs.**
