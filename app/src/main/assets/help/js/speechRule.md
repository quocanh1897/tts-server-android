# Speech rules
> Speech rules split the original text into segments and mark each one with a tag. TTS Server then matches a config by tag and synthesizes speech.

## Built-in rule example:

```javascript
let SpeechRuleJS = {
    name: "Narration/Dialogue",
    id: "ttsrv.multi_voice",
    author: "TTS Server",
    version: 4,

    // Map: the key is the tag id, the value is the label shown in the tag dropdown
    tags: {narration: "Narration", dialogue: "Dialogue"},

    // text is the original, unprocessed text
    // Returns a List:
    // [
    //     {text: "This is narration", tag: "narration"},
    //     {text: "This is dialogue", tag: "dialogue"}
    // ]
    handleText(text) {
        const list = [];
        let tmpStr = "";
        let endTag = "narration";

        text.split("").forEach((char, index) => {
            tmpStr += char;

            if (char === '\u201c') {
                endTag = "dialogue";
                list.push({text: tmpStr, tag: "narration"});
                tmpStr = "";
            } else if (char === '\u201d') {
                endTag = "narration";
                tmpStr = tmpStr.slice(0, -1)
                list.push({text: tmpStr, tag: "dialogue"});
                tmpStr = "";
            } else if (index === text.length - 1) {
                list.push({text: tmpStr, tag: endTag});
            }
        });

        return list;
    },

    // After handleText, split each returned element's text into sentences.
    // Returns a list of strings.
    splitText(text) {
        let separatorStr = "\u3002\uFF1F?\uFF01!;\uFF1B"

        let list = []
        let tmpStr = ""
        text.split("").forEach((char, index) => {
            tmpStr += char

            if (separatorStr.includes(char)) {
                list.push(tmpStr)
                tmpStr = ""
            } else if (index === text.length - 1) {
                list.push(tmpStr);
            }
        })

        return list.filter(item =>  item.replace(/[\u201c\u201d]/g, '').trim().length > 0);
    }

};


```
