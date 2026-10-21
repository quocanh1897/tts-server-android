package com.github.jing332.tts

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.jing332.database.entities.plugin.Plugin
import com.github.jing332.database.entities.systts.source.PluginTtsSource
import com.github.jing332.tts.synthesizer.SystemParams
import com.github.jing332.tts.speech.plugin.PluginTtsProvider
import com.github.jing332.tts.speech.plugin.engine.TtsPluginUiEngineV2
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TtsPluginEngineTest {
    private val context: Context by lazy {
        InstrumentationRegistry.getInstrumentation().context
    }

    @Test
    fun ttsrv() {
        val code = """
            let token = ttsrv.userVars.token
            console.log(token)
            const PluginJS = {
                'name': 'lingban',
                'id': 'lingban.cn',
                'author': 'jing',
                'version': 2,
                'vars': {
                    token: { label: "Token", hint: "Your token" },
                },
                'onLoad': function() {
                    console.log("onLoad")
                    console.log(ttsrv.userVars)
                },
                
                // Called when getAudio() is absent
                'getAudioV2': function(request, callback){
                    console.log(request)    
                },
                
                'getAudio': function(text, locale, voice, rate, volume, pitch) {
                    let reqUrl = 'https://paas.lingban.cn/tts/v1/audition'
                    let reqHeaders = {
                        'content-type': 'application/json;charset=UTF-8'
                    }
                    let reqBody = {
                        'text': text,
                        'speaker': voice,
                        'volume': volume,
                        'speed': rate,
                        'pitch': pitch
                    }
                    reqBody = JSON.stringify(reqBody)
                    
                    let resp = ttsrv.httpPost(reqUrl, reqBody, reqHeaders)
                    return resp.body().byteStream()
                }
            } 

            let EditorJS = {
                // Called when saving on the TTS edit screen
                'getAudioSampleRate': function(locale, voice) {
                    let audio = PluginJS.getAudio('test', locale, voice, 50, 50, 50)
                    let rate = ttsrv.getAudioSampleRate(audio)
            
                    return rate
                },
            
                'onLoadData': function() {
            
                },
            
                'getLocales': function() {
                    return ['zh-CN', 'en-US']
                },
            
                'getVoices': function(locale) {
                    console.log(typeof locale)
                    if (locale == 'zh-CN') {
                        return {'yanyan':'Yanyan [warm sweet female voice]','lulu':'Lulu [bright lively female voice]','qiqi':'Qiqi [fresh crisp female voice]','nana':'Nana [soft calm female voice]','xiaocheng':'Xiaocheng [flowing songlike female voice]','xiaoyao':'Xiaoyao [sweet lively female voice]','chenyu':'Chenyu [gentle polite female voice]','lingfeng':'Lingfeng [graceful female voice]','ruirui':'Ruirui [steady calm female voice]','luyao':'Luyao [melodious female voice]','linglong':'Linglong [light sweet female voice]','yurou':'Yurou [light cheerful female voice]','siqi':'Siqi [melodious female voice]','chenchen':'Chenchen [enthusiastic female voice]','qiulian':'Qiulian [breezy relaxed female voice]','xintong':'Xintong [clear pleasant female voice]','yiyi':'Yiyi [bright pleasant female voice]','wenhui':'Wenhui [smooth polished female voice]','xiaoxiao':'Xiaoxiao [trustworthy warm female voice]','xiaofang':'Xiaofang [innocent lively female voice]','xiaoai':'Xiaoai [sincere friendly female voice]','nainai':'Nainai [natural genuine female voice]','chengmiao':'Chengmiao [brisk spirited female voice]','zhengcheng':'Zhengcheng [soft firm male voice]','zili':'Zili [quiet refined male voice]','ningning':'Ningning [warm gentle male voice]','zhentian':'Zhentian [open easygoing male voice]','bufan':'Bufan [magnetic rich male voice]','wenxi':'Wenxi [sunny natural male voice]','tiantian':'Tiantian [steady deep male voice]','lingyu':'Lingyu [mild polished male voice]','wusong':'Wusong [bright serious male voice]','kunde':'Kunde [deep steady male voice]','jinyan':'Jinyan [clear precise male voice]','shenzhi':'Shenzhi [tense pressing male voice]','ruyan':'Ruyan [serious formal male voice]','jianming':'Jianming [fantasy novel]','yuzhang':'Yuzhang [fantasy novel]','chuanqi':'Chuanqi [game novel]','lingmeng':'Lingmeng [romance novel]','shenghun':'Shenghun [fantasy novel]','yizhou':'Yizhou [urban novel]','youlan':'Youlan [romance novel]','hongmang':'Hongmang [fantasy novel]','youran':'Youran [fantasy novel]','shanyu':'Shanyu [fantasy novel]','mengjing':'Mengjing [urban novel]','yunsong':'Yunsong [fantasy novel]','haohan':'Haohan [urban novel]','linger':'Linger [childlike female voice]','yaoyao':'Yaoyao [childlike male voice]','subo':'Subo [news male voice]','mina':'Mina [news female voice]','linghui':'Linghui [news female voice]','lingyin':'Lingyin [news female voice]','shuangshuang':'Shuangshuang [news male voice]','jeff':'Jeff [English news]','rose':'Rose [English novel]','julia':'Julia [English novel]','jack':'Jack [English novel]','liangshu':'Liangshu [celebrity voice]','xiaosong':'Xiaosong [celebrity voice]','laoyi':'Laoyi [celebrity voice]','dafei':'Dafei [celebrity voice]','shishen':'Shishen [celebrity voice]','wansheng':'Wansheng [celebrity voice]','kaige':'Kaige [celebrity voice]','guobao':'Guobao [celebrity voice]','jiange':'Jiange [celebrity voice]'}
                    }
                },
            
                'onLoadUI': function(ctx, linearLayout) {
            
                }
            }
        """.trimIndent()

        val plugin = Plugin(code = code, userVars = mapOf("token" to "value-token"))
        val tte = TtsPluginUiEngineV2(context, plugin)
 //        tte.onLoadData()
//        val locales = tte.getLocales()
        val voices = tte.getVoices("zh-CN")


//        tte.getAudio(
//            text = "The river falls from the sky and rushes to the sea.",
//            locale = "zh-CN",
//            voice = "test-li-bai",
//            rate = 1.2f,
//            volume = 1.25f,
//            pitch = 1.80f
//        )

    }


    @Test
    fun cancelable() = runBlocking {
        val code = """
        const PluginJS = {
            'name': 'lingban',
            'id': 'lingban.cn',
            'author': 'jing',
            'version': 2,
            'vars': {
                bearer: { label: "token", hint: "Your token" },
            },
            'onLoad': function() {
                console.log("onLoad")
            },
            
            // Called when getAudio() is absent
            'getAudioV2': function(request, callback){
                console.log(request)    
            },
            
            'getAudio': function(text, locale, voice, rate, volume, pitch) {
                let reqUrl = 'https://paas.lingban.cn/tts/v1/audition'
                let reqHeaders = {
                    'content-type': 'application/json;charset=UTF-8'
                }
                let reqBody = {
                    'text': text,
                    'speaker': voice,
                    'volume': volume,
                    'speed': rate,
                    'pitch': pitch
                }
                reqBody = JSON.stringify(reqBody)
                console.log(reqBody)

                java.lang.Thread.sleep(5000)
                let resp = ttsrv.httpPost(reqUrl, reqBody, reqHeaders)
                return resp.body().byteStream()
            }
            
       } 
        """.trimIndent()
        val ttsEngineService = object : PluginTtsProvider(context, Plugin(code = code)) {
        }
        val job = launch(Dispatchers.IO) {
            ttsEngineService.onInit()
            val inputStream = ttsEngineService.getStream(
                SystemParams(text = "Hello, I am androidTest"),
                PluginTtsSource(pluginId = "", locale = "zh-CN", voice = "")
            )
        }
        delay(1000)
        job.cancel()
    }

    @Test
    fun testUiJsImporter() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val code = """
            LinearLayout.VERTICAL
        """.trimIndent()
        val engine = TtsPluginUiEngineV2(context, Plugin(code = code))

     }

}