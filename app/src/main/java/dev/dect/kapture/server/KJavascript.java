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

    public void addScreenshot() {
        SCRIPT += "const screenshots=document.getElementById(\"screenshots\"),screenshotsTitle=document.getElementById(\"screenshots-title\");document.getElementById(\"closeScreenshots\").addEventListener(\"click\",()=>screenshots.style.display=\"none\"),window.addEventListener(\"load\",()=>{document.querySelectorAll(\".screenshot\").forEach(e=>{e.addEventListener(\"click\",()=>{document.querySelectorAll(\"#screenshots-body .row\").forEach(e=>e.remove()),JSON.parse(e.dataset.screenshots).forEach(e=>{var t=window.location.origin+window.location.pathname;\"/\"===t[t.length-1]&&(t=t.substring(0,t.length-1));let s=document.createElement(\"div\");s.className=\"row\";let n=document.createElement(\"a\");n.className=\"download\",n.href=t+e.l;let r=document.createElement(\"span\");r.innerHTML=e.n,s.appendChild(n),s.appendChild(r),screenshotsTitle.after(s)}),screenshots.style.display=\"block\"})})});";
    }

    public void addList() {
        SCRIPT += "window.addEventListener(\"load\",function(){var l=window.location.origin+window.location.pathname;if(l[l.length-1]===\"/\"){l=l.substring(0,l.length-1)}document.querySelectorAll(\"a\").forEach(function(a){a.href=l+a.getAttribute(\"href\")})},false);";

        addPlayers();
    }

    private void addPlayers() {
        SCRIPT += "const video=document.getElementById(\"video\"),audio=document.getElementById(\"audio\"),players=document.getElementById(\"players\");function addPlayerListener(e){e.addEventListener(\"click\",()=>{let a=\"video\"===e.dataset.type;var l=window.location.origin+window.location.pathname;\"/\"===l[l.length-1]&&(l=l.substring(0,l.length-1)),l+=e.dataset.href,a?(video.style.display=\"block\",audio.style.display=\"none\",video.src=l,video.play()):(video.style.display=\"none\",audio.style.display=\"block\",audio.src=l,audio.play()),players.style.display=\"block\"})}document.getElementById(\"closePlayers\").addEventListener(\"click\",()=>{video.pause(),audio.pause(),players.style.display=\"none\"}),window.addEventListener(\"load\",()=>{document.querySelectorAll(\".play\").forEach(e=>addPlayerListener(e))});";
    }

    public void setLogin() {
        SCRIPT = "window.addEventListener(\"load\",function(){var e=document.querySelectorAll(\".pass\");e[0].focus(),e.forEach(function(e){e.addEventListener(\"focus\",function(e){e.target.value=\"\"}),e.addEventListener(\"input\",function(e){if(e.target.value.length>1)e.target.value=\"\";else try{e.target.nextElementSibling.focus()}catch(t){}}),e.addEventListener(\"keydown\",function(e){8===e.keyCode&&(e.target.value=\"\",e.target.previousElementSibling.focus())})}),e[e.length-1].addEventListener(\"input\",function(t){var n=\"\";e.forEach(function(e){n+=e.value}),n.length==e.length&&(t.target.blur(),document.getElementById(\"p\").value=n,document.getElementById(\"l\").submit(),document.getElementById(\"h\").classList.add(\"s\"),e.forEach(function(e){e.value=\"\",e.placeholder=\"*\"}))})});";
    }
}
