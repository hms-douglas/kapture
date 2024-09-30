package dev.dect.kapture.server;

public class KJavascript {
    private String SCRIPT = "";

    public KJavascript() {}

    public String getJavascript() {
        return SCRIPT;
    }

    public void addExtra() {
        SCRIPT += "const extras=document.getElementById(\"extras\"),extrasTitle=document.getElementById(\"extras-title\");document.getElementById(\"closeExtras\").addEventListener(\"click\",()=>extras.style.display=\"none\"),window.addEventListener(\"load\",()=>{document.querySelectorAll(\".extra\").forEach(e=>{e.addEventListener(\"click\",()=>{document.querySelectorAll(\"#extras-body .row\").forEach(e=>e.remove()),JSON.parse(e.dataset.extras).forEach(e=>{var t=window.location.origin+window.location.pathname;\"/\"===t[t.length-1]&&(t=t.substring(0,t.length-1));let a=document.createElement(\"div\");a.className=\"row\";let l=document.createElement(\"div\");l.className=\"play\";l.dataset.href = e.l;l.dataset.type = e.l.includes(\".mp4\") ? \"video\" : \"audio\";addPlayerListener(l);let r=document.createElement(\"a\");r.className=\"download\",r.href=t+e.l;let n=document.createElement(\"span\");n.innerHTML=e.n,a.appendChild(l),a.appendChild(r),a.appendChild(n),extrasTitle.after(a)}),extras.style.display=\"block\"})})});";
    }

    public void addList() {
        SCRIPT += "window.addEventListener(\"load\",function(){var l=window.location.origin+window.location.pathname;if(l[l.length-1]===\"/\"){l=l.substring(0,l.length-1)}document.querySelectorAll(\"a\").forEach(function(a){a.href=l+a.getAttribute(\"href\")})},false);";

        addPlayers();
    }

    private void addPlayers() {
        SCRIPT += "const video=document.getElementById(\"video\"),audio=document.getElementById(\"audio\"),players=document.getElementById(\"players\");function addPlayerListener(e){e.addEventListener(\"click\",()=>{let a=\"video\"===e.dataset.type;var l=window.location.origin+window.location.pathname;\"/\"===l[l.length-1]&&(l=l.substring(0,l.length-1)),l+=e.dataset.href,a?(video.style.display=\"block\",audio.style.display=\"none\",video.src=l,video.play()):(video.style.display=\"none\",audio.style.display=\"block\",audio.src=l,audio.play()),players.style.display=\"block\"})}document.getElementById(\"closePlayers\").addEventListener(\"click\",()=>{video.pause(),audio.pause(),players.style.display=\"none\"}),window.addEventListener(\"load\",()=>{document.querySelectorAll(\".play\").forEach(e=>addPlayerListener(e))});";
    }
}
