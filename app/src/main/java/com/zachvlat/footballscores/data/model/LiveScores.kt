package com.zachvlat.footballscores.data.model

data class LiveScoresResponse(
    val Ts: Long,
    val Stages: List<Stage>
)

data class Stage(
    val Sid: String,
    val Snm: String,
    val Scd: String,
    val Cnm: String,
    val CnmT: String,
    val Csnm: String,
    val Ccd: String,
    val CompId: String,
    val CompN: String,
    val CompUrlName: String,
    val CompD: String,
    val CompST: String,
    val Scu: Int,
    val badgeUrl: String?,
    val firstColor: String?,
    val Events: List<Event>
)

data class Event(
    val Eid: String,
    val Pids: Map<String, String>,
    val Media: Media?,
    val Tr1: String,
    val Tr2: String,
    val Trh1: String?,
    val Trh2: String?,
    val Tr1OR: String?,
    val Tr2OR: String?,
    val T1: List<Team>,
    val T2: List<Team>,
    val Eps: String,
    val Esid: Int,
    val Epr: Int,
    val Ecov: Int,
    val ErnInf: String?,
    val Ewt: Int?,
    val Et: Int,
    val Esd: Long,
    val EO: Long,
    val EOX: Long,
    val Spid: Int,
    val Pid: Int
)

data class Team(
    val ID: String,
    val Nm: String,
    val Img: String?,
    val NewsTag: String?,
    val Abr: String,
    val Fc: String?,
    val Sc: String?
) {
    fun getTeamImageUrl(): String? {
        return Img?.let { 
            if (it.startsWith("enet")) {
                "https://storage.livescore.com/images/team/medium/$it"
            } else {
                it
            }
        }
    }
}

data class Media(
    val `112`: List<MediaItem>?,
    val `29`: List<MediaItem>?
)

data class MediaItem(
    val eventId: String?,
    val provider: String?,
    val type: String,
    val thumbnail: String?,
    val allowedCountries: List<String>?,
    val streamhls: String?,
    val deniedCountries: List<String>?,
    val ageRestricted: String?,
    val beforeStartTime: Int?,
    val afterEndTime: Int?
)