package dev.dect.kapture.server;

public class KJavascript {
    private String SCRIPT = "";

    public KJavascript() {}

    public String getJavascript() {
        return SCRIPT;
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
