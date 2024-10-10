package dev.dect.kapture.server;

import android.content.Context;

public class KCss {
    private final Context CONTEXT;

    private String STYLE = "";

    public KCss(Context ctx) {
        this.CONTEXT = ctx;
    }

    public String getCss() {
        addTheme();

        addDefault();

        return STYLE;
    }

    private void setDefaultErrorMessage() {
        STYLE = "#message,#number{display:block;text-align:center}#containerError{width:fit-content;height:fit-content;background-color:var(--c_c4);padding:60px;border-radius:var(--d_radius);position:fixed;top:0;bottom:0;right:0;left:0;margin:auto;box-shadow:rgba(17,17,26,.05) 0 1px 0,rgba(17,17,26,.1) 0 0 8px}#number{font-size:2.5rem;color:var(--c_text);font-weight:700;margin-bottom:15px}#message{font-size:1.2rem}";
    }

    public void setList() {
        addHeader();

        addList();

        addPlayers();
    }

    public void addExtra() {
        STYLE += "#closeExtras,#extras-title{font-weight:700;font-size:1.05rem}#closeExtras:hover,#extras-body{border-radius:var(--d_radius)}.extra{background-image:url(\"data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24'%3E%3Cpath d='M19.2186,4.5979C17.2306,2.6169 13.9936,2.6169 12.0056,4.5979L3.5076,13.0659C3.2136,13.3579 3.2126,13.8329 3.5056,14.1259C3.7976,14.4199 4.2726,14.4199 4.5666,14.1279L13.0646,5.6599C14.4686,4.2599 16.7546,4.2599 18.1586,5.6599C18.8386,6.3379 19.2136,7.2389 19.2136,8.1969C19.2136,9.1549 18.8386,10.0559 18.1596,10.7339L9.9216,18.9409C9.3236,19.5379 8.3506,19.5369 7.7516,18.9409C7.4626,18.6519 7.3026,18.2689 7.3026,17.8619C7.3026,17.4539 7.4616,17.0709 7.7506,16.7829L15.2806,9.2819C15.5726,8.9889 15.5736,8.5139 15.2816,8.2209C14.9896,7.9269 14.5146,7.9259 14.2216,8.2189L6.6926,15.7199C6.1186,16.2919 5.8026,17.0529 5.8026,17.8619C5.8036,18.6699 6.1186,19.4309 6.6926,20.0019C7.2836,20.5929 8.0606,20.8879 8.8376,20.8879C9.6146,20.8879 10.3906,20.5929 10.9816,20.0019L19.2186,11.7949C20.1826,10.8339 20.7136,9.5559 20.7136,8.1969C20.7136,6.8369 20.1826,5.5589 19.2186,4.5979' stroke-width='1' stroke='%2300000000' id='path_0'/%3E%3C/svg%3E\")}#extras{display:none;position:fixed;top:0;left:0;width:100%;height:100%;background-color:#0000005c;backdrop-filter:blur(1px)}#extras-body{position:absolute;bottom:20px;left:0;right:0;margin:auto;width:calc(37.5% - 40px);background-color:var(--c_c4);padding:20px}#extras-title,.row{display:block;margin-bottom:18px}.row *{display:inline-block;vertical-align:top}.row>span{padding-left:8px;height:var(--d_icon);line-height:var(--d_icon)}#closeExtras{text-align:center;padding:12px 0;cursor:pointer}#closeExtras:hover{background-color:#00000020}@media (min-width:100px){#extras-body{width:calc(100% - 60px);bottom:10px}}@media (min-width:500px){#extras-body{width:calc(80% - 60px);bottom:10px}}@media (min-width:800px){#extras-body{width:calc(60% - 60px);bottom:10px}}@media (min-width:1000px){#extras-body{width:calc(50% - 60px);bottom:10px}}@media (min-width:1200px){#extras-body{width:calc(40% - 60px);bottom:10px;max-width:500px}}";
    }

    public void set403() {
        setDefaultErrorMessage();
    }

    public void set500() {
        setDefaultErrorMessage();
    }

    private void addHeader() {
        STYLE += "#title{width:100%;height:25vh;margin-bottom:15px;position:relative}#title>div{position:absolute;top:0;bottom:0;left:0;right:0;margin:auto;height:fit-content;text-align:center}#title>div span{display:block}#title>div span:first-child{font-size:2rem;margin-bottom:5px}#title>div span:nth-child(2){color:var(--c_c3)}";
    }

    private void addList() {
        STYLE += "table{width:100%;border-collapse:collapse;border-spacing:0}td,th{text-align:start;padding:7px;border-bottom:2px solid var(--c_c1)}th{font-weight:bolder}td{border-bottom-style:dotted;transition:.2s ease-in-out;height:calc(var(--d_icon) + 20px)}tr:hover td{background-color:var(--c_c2)}td:last-of-type,th:last-of-type{min-width:calc((var(--d_icon) + 10px) * 3 + 5px);padding:0;text-align:end}.download,.extra,.play{display:inline-block;height:var(--d_icon);width:var(--d_icon);margin-right:10px;cursor:pointer;filter:var(--i_filter)}.download:hover,.extra:hover,.play:hover{filter:invert(27%) sepia(65%) saturate(2121%) hue-rotate(341deg) brightness(102%) contrast(97%);opacity:1}.play{background-image:url(\"data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24'%3E%3Cpath d='M8.762,19.519L18.011,13.476C19.148,12.82 19.148,11.18 18.011,10.524L8.762,4.481C7.626,3.825 6.205,4.645 6.205,5.957L6.205,18.042C6.205,19.355 7.626,20.175 8.762,19.519' stroke-width='1' stroke='%2300000000' id='path_0'/%3E%3C/svg%3E\")}.download{background-image:url(\"data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24'%3E%3Cpath d='M18.1535,19.7385C18.5675,19.7385 18.9035,20.0745 18.9035,20.4885C18.9035,20.9025 18.5675,21.2385 18.1535,21.2385L18.1535,21.2385L5.8465,21.2385C5.4325,21.2385 5.0965,20.9025 5.0965,20.4885C5.0965,20.0745 5.4325,19.7385 5.8465,19.7385L5.8465,19.7385ZM12.0091,2.7615C12.4231,2.7615 12.7591,3.0975 12.7591,3.5115L12.7591,3.5115L12.7591,15.5725L16.6171,11.5485C16.9031,11.2485 17.3781,11.2385 17.6781,11.5255C17.9771,11.8115 17.9861,12.2865 17.7001,12.5865L17.7001,12.5865L13.1611,17.3215C12.8601,17.6355 12.4361,17.8165 12.0001,17.8165C11.5641,17.8165 11.1411,17.6355 10.8391,17.3215L10.8391,17.3215L6.3001,12.5865C6.0141,12.2865 6.0241,11.8115 6.3221,11.5255C6.6221,11.2385 7.0961,11.2485 7.3831,11.5485L7.3831,11.5485L11.2591,15.5915L11.2591,3.5115C11.2591,3.0975 11.5951,2.7615 12.0091,2.7615Z' stroke-width='1' stroke='%2300000000' id='path_0'/%3E%3C/svg%3E\")}tr:last-of-type td{border-bottom:none}@media (max-width:860px){*{font-size:1rem}body{margin:5px}td:nth-child(2),td:nth-child(3),td:nth-child(4),td:nth-child(5),th{display:none}td{border-bottom-width:1px;border-bottom-style:solid}#title{height:39.67vh;margin-bottom:0}tr:hover td{background-color:transparent}#title>div span:first-child{font-size:2.2rem}#extras{backdrop-filter:blur(0.5px)}.row>span{line-height:calc(var(--d_icon) + 5px)}}.download,.extra,.play{opacity:.6}";
    }

    private void addPlayers() {
        STYLE += "#audio,#closePlayers,#video{position:fixed;left:0;right:0;margin:auto}#players{display:none;position:fixed;top:0;left:0;width:100%;height:100%;background-color:#0000005c;backdrop-filter:blur(5px)}#audio,#video{display:none;top:0;bottom:0;width:85%;max-height:85%;object-fit:contain}#closePlayers{bottom:20px;width:fit-content;padding:12px 50px;text-align:center;font-weight:700;font-size:1.05rem;cursor:pointer;background-color:var(--c_c0);border-radius:var(--d_radius)}#closePlayers:hover{background-color:#00000020}";
    }

    private void addTheme() {
        if(CONTEXT.getResources().getConfiguration().isNightModeActive()) {
            STYLE = ":root{--c_c0:#000000;--c_c1:#3B3B40;--c_c2:#b6b6b625;--c_c3:#979797;--c_c4:#252525;--c_text:#EDEDED;--i_filter:invert(100%) sepia(8%) saturate(42%) hue-rotate(37deg) brightness(109%) contrast(86%)}" + STYLE;
        } else {
            STYLE = ":root{--c_c0:#F6F6F6;--c_c1:#808080;--c_c2:#6e6e6e25;--c_c3:#8E8E8E;--c_c4:#E8E8E8;--c_text:#020202;--i_filter:none}" + STYLE;
        }
    }

    private void addDefault() {
        STYLE = ":root{--d_radius:20px;--d_icon:30px}*{color:var(--c_text);font-family:Arial,Helvetica,sans-serif;font-size:1.06rem}html{background-color:var(--c_c0)}body{margin:20px}" + STYLE;
    }
}
