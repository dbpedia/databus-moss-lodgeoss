package org.dbpedia.databus.moss.annotation;

public class SVGBuilder {

    public static String svgString2dec = "<?xml version=\"1.0\"?>\n" +
            "<svg xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" width=\"42\" height=\"20\" role=\"img\" aria-label=\"&#x270E;: #NO\">\n" +
            "  <title>&#x270E;: #NO</title>\n" +
            "  <linearGradient id=\"s\" x2=\"0\" y2=\"100%\">\n" +
            "    <stop offset=\"0\" stop-color=\"#bbb\" stop-opacity=\".1\"/>\n" +
            "    <stop offset=\"1\" stop-opacity=\".1\"/>\n" +
            "  </linearGradient>\n" +
            "  <clipPath id=\"r\">\n" +
            "    <rect width=\"42\" height=\"20\" rx=\"3\" fill=\"#fff\"/>\n" +
            "  </clipPath>\n" +
            "  <g clip-path=\"url(#r)\">\n" +
            "    <rect width=\"19\" height=\"20\" fill=\"#555\"/>\n" +
            "    <rect x=\"19\" width=\"23\" height=\"20\" fill=\"#007ec6\"/>\n" +
            "    <rect width=\"42\" height=\"20\" fill=\"url(#s)\"/>\n" +
            "  </g>\n" +
            "  <g fill=\"#fff\" text-anchor=\"middle\" font-family=\"Verdana,Geneva,DejaVu Sans,sans-serif\" text-rendering=\"geometricPrecision\" font-size=\"110\">\n" +
            "    <text aria-hidden=\"true\" x=\"105\" y=\"190\" fill=\"#010101\" fill-opacity=\".3\" transform=\"scale(.1)\" textLength=\"90\" font-size=\"2em\">&#x270E;</text>\n" +
            "    <text x=\"105\" y=\"180\" transform=\"scale(.1)\" fill=\"#fff\" textLength=\"90\" font-size=\"2em\">&#x270E;</text>\n" +
            "    <text aria-hidden=\"true\" x=\"295\" y=\"150\" fill=\"#010101\" fill-opacity=\".3\" transform=\"scale(.1)\" textLength=\"130\">#NO</text>\n" +
            "    <text x=\"295\" y=\"140\" transform=\"scale(.1)\" fill=\"#fff\" textLength=\"130\">#NO</text>\n" +
            "  </g>\n" +
            "</svg>\n";

    public static String api_demo_svg_base = "<svg\n" +
            "    xmlns=\"http://www.w3.org/2000/svg\"\n" +
            "    xmlns:xlink=\"http://www.w3.org/1999/xlink\" width=\"52\" height=\"20\" role=\"img\" aria-label=\"\uD83D\uDCF0: #NO\">\n" +
            "    <title>\uD83D\uDCF0: #NO</title>\n" +
            "    <linearGradient id=\"s\" x2=\"0\" y2=\"100%\">\n" +
            "        <stop offset=\"0\" stop-color=\"#bbb\" stop-opacity=\".1\"/>\n" +
            "        <stop offset=\"1\" stop-opacity=\".1\"/>\n" +
            "    </linearGradient>\n" +
            "    <clipPath id=\"r\">\n" +
            "        <rect width=\"52\" height=\"20\" rx=\"3\" fill=\"#fff\"/>\n" +
            "    </clipPath>\n" +
            "    <g clip-path=\"url(#r)\">\n" +
            "        <rect width=\"21\" height=\"20\" fill=\"#555\"/>\n" +
            "        <rect x=\"21\" width=\"31\" height=\"20\" fill=\"blueviolet\"/>\n" +
            "        <rect width=\"52\" height=\"20\" fill=\"url(#s)\"/>\n" +
            "    </g>\n" +
            "    <g fill=\"#fff\" text-anchor=\"middle\" font-family=\"Verdana,Geneva,DejaVu Sans,sans-serif\" text-rendering=\"geometricPrecision\" font-size=\"110\">\n" +
            "        <text aria-hidden=\"true\" x=\"115\" y=\"150\" fill=\"#010101\" fill-opacity=\".3\" transform=\"scale(.1)\" textLength=\"110\">\uD83D\uDCF0</text>\n" +
            "        <text x=\"115\" y=\"140\" transform=\"scale(.1)\" fill=\"#fff\" textLength=\"110\">\uD83D\uDCF0</text>\n" +
            "        <text aria-hidden=\"true\" x=\"355\" y=\"150\" fill=\"#010101\" fill-opacity=\".3\" transform=\"scale(.1)\" textLength=\"210\">#NO</text>\n" +
            "        <text x=\"355\" y=\"140\" transform=\"scale(.1)\" fill=\"#fff\" textLength=\"210\">#NO</text>\n" +
            "    </g>\n" +
            "</svg>";

    static String gen(Integer noAnnotations) {
        return svgString2dec.replaceAll("#NO",String.valueOf(noAnnotations));
    }
}
