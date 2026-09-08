package com.example.mviplayer.data

/**
 * 歌曲数据实体模型
 */
data class Song(
    val id: String,
    val title: String,
    val artist: String,
    val mediaUrl: String
)


object SampleMusicData {
    val songs = listOf(
        Song(
            id = "1",
            title = "诀别书",
            artist = "邓垚",
            mediaUrl = "https://music.163.com/song/media/outer/url?id=2038191895.mp3"
        ),
        Song(
            id = "2",
            title = "神々の祈り",
            artist = "舞風 / 沙紗飛鳥",
            mediaUrl = "https://music.163.com/song/media/outer/url?id=785106.mp3"
        ),
        Song(
            id = "3",
            title = "ミゼラブルの雫",
            artist = "幽閉サテライト",
            mediaUrl = "https://music.163.com/song/media/outer/url?id=869351.mp3"
        ),
        Song(
            id = "4",
            title = "亡き王女の為のセプテット",
            artist = "ついったー東方部",
            mediaUrl = "https://music.163.com/song/media/outer/url?id=437802186.mp3"
        ),
        Song(
            id = "5",
            title = "Clover",
            artist = "大原ゆい子",
            mediaUrl = "https://music.163.com/song/media/outer/url?id=2061929240.mp3"
        )
    )
}