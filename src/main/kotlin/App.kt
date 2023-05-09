
import csstype.Display
import csstype.NamedColor
import kotlinx.coroutines.async
import react.*
import react.dom.*
import kotlinx.browser.window
import kotlinx.coroutines.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import emotion.react.css
import csstype.Position
import csstype.px
import react.dom.html.InputType
import react.dom.html.ReactHTML
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.h1
import react.dom.html.ReactHTML.h3
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.p
import react.dom.html.ReactHTML.img
import react.dom.html.ReactHTML.input

val mainScope = MainScope()

val App = FC<Props> {
    var currentVideo: Video? by useState(null)
    var unwatchedVideos: List<Video> by useState(emptyList())
    var watchedVideos: List<Video> by useState(emptyList())
    var mainVideos: List<Video> by useState(emptyList())
    var title:String? by useState();
    var speaker:String? by useState();

    useEffectOnce {
        mainScope.launch {
            unwatchedVideos = fetchVideos()
            mainVideos = unwatchedVideos;
        }
    }
    // typesafe HTML goes here, starting with the first h1 tag!
    h1 {
        +"KotlinConf Explorer"
    }

    h3 {
        +"Search for video Explorer"
    }

    h3 {
        +"Enter title "
    }
    div{
        input {
        type=InputType.text
        value = title
        onChange =
                { event -> title = event.target.value;

            }
        }

        button {
            css {
                display = Display.block
            }
            +"Search by title"

            onClick = {
                mainVideos = searchByTitle(title,unwatchedVideos);
            }

        }
        h3 {
            +"Enter speaker "
        }
        input {
            type=InputType.text
            value = speaker
            onChange =
                { event -> speaker = event.target.value;

                }
        }
        button {
            css {
                display = Display.block
            }
            +"Search by Speaker"

            onClick = {
                //unwatchedVideos = emptyList();
                mainVideos = searchBySpeaker(speaker,unwatchedVideos);
            }
        }

    }

    div {
        h3 {
            +"Videos result"
        }
        VideoList {
            videos = mainVideos
            selectedVideo = currentVideo
            onSelectVideo = { video ->
                currentVideo = video
            }
        }
        h3 {
            +"Videos to watch"
        }
        VideoList {
            videos = unwatchedVideos
            selectedVideo = currentVideo
            onSelectVideo = { video ->
                currentVideo = video
            }
        }
        h3 {
            +"Videos watched"
        }
        VideoList {
            videos = watchedVideos
            selectedVideo = currentVideo
            onSelectVideo = { video ->
                currentVideo = video
            }
        }
    }
    currentVideo?.let { curr ->
        VideoPlayer {
            video = curr
            unwatchedVideo = curr in unwatchedVideos
            onWatchedButtonPressed = {
                if (video in unwatchedVideos) {
                    unwatchedVideos = unwatchedVideos - video
                    watchedVideos = watchedVideos + video
                } else {
                    watchedVideos = watchedVideos - video
                    unwatchedVideos = unwatchedVideos + video
                }
            }
        }
    }
}
suspend fun fetchVideo(id: Int): Video {
    val response = window
        .fetch("https://my-json-server.typicode.com/kotlin-hands-on/kotlinconf-json/videos/$id")
        .await()
        .text()
        .await()
    return Json.decodeFromString(response)
}

suspend fun fetchVideos(): List<Video> = coroutineScope {
    (1..25).map { id ->
        async {
            fetchVideo(id)
        }
    }.awaitAll()
}

fun searchByTitle(title:String?,unwatchedVideos:List<Video>): List<Video>{
    val resultList = ArrayList<Video>()
    for (video in unwatchedVideos) {
    if(video.title == title){
        resultList.add(video);
    }
    }
    return resultList;
}

fun searchBySpeaker(speaker:String?,unwatchedVideos:List<Video>): List<Video>{
    val resultList = ArrayList<Video>()
    for (video in unwatchedVideos) {
        if(video.speaker == speaker){
            resultList.add(video);
        }
    }
    return resultList;
}