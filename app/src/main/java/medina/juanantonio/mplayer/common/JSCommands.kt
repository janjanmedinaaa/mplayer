package medina.juanantonio.mplayer.common

import medina.juanantonio.mplayer.data.models.FEpisode

class JSCommands {

    companion object {

        fun getHTMLDocument(): String {
            return "(function() { return ('<html>'+document.getElements" +
                    "ByTagName('html')[0].innerHTML+'</html>'); })();"
        }

        fun clickAutoPlayButton(): String {
            return "javascript:(function(){" +
                    "l=document.querySelector('div[data-name=\"autoplay\"] > i.fa-circle');" +
                    "l.click();" +
                    "})()"
        }

        fun onlyShowIFramePlayer(): String {
            return "javascript:(function(){" +
                    "h=document.getElementsByTagName('header')[0];" +
                    "h.style=\"visibility:collapse\";" +
                    "i=document.querySelector('div#watch > div.container');" +
                    "i.style=\"visibility:collapse\";" +
                    "j=document.querySelector('div.nav');" +
                    "j.style=\"visibility:collapse\";" +
                    "k=document.getElementsByTagName('footer')[0];" +
                    "k.style=\"visibility:collapse\";" +
                    "l=document.querySelector('div#player > iframe[allow" +
                    "=\"autoplay; fullscreen\"]'); l.style=\"position:fixed; " +
                    "top:0; left:0; bottom:0; right:0; border:none; margin:0; " +
                    "padding:0; overflow:hidden; z-index:999999; " +
                    "width:100%; height:100%;\";" +
                    "})()"
        }

        fun clickEpisodeItem(fEpisode: FEpisode): String {
            return "javascript:(function(){" +
                    "l=document.getElementById('${fEpisode.sourceDataId}')" +
                    ".querySelector('li > a[data-kname=\"${fEpisode.season}:${fEpisode.episode}\"]');" +
                    "l.click();" +
                    "})()"
        }
    }
}