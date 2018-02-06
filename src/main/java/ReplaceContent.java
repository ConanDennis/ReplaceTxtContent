import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class ReplaceContent {

    private static final String propertyUrl = "http://qfc.qunar.com/homework/sdxl_prop.txt";
    private static final String articleUrl = "http://qfc.qunar.com/homework/sdxl_template.txt";
    private static final String outputFile = "./sdxl.txt";

    private static List<String> sentenceList = Lists.newLinkedList();
    private static Map<String, String> propertyMap = Maps.newHashMap();

    public static void main(String[] args) {

        try {
            replaceText();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void replaceText() throws IOException {
        getProperty();
        List<String> articleLines = readText(articleUrl);
        StringBuilder sb = new StringBuilder();
        int propertyRow = 0;
        for (String line : articleLines) {
            line = line.trim();
            if (line.equals("")) {
                sb.append("\n");
                continue;
            }

            int prev = -1;
            int last = 0;

            StringBuilder tmpLine = new StringBuilder();

            List<FunctionDesc> functionList = analysisFunctionsInLine(line);

            for (FunctionDesc func : functionList) {
                tmpLine.append(line.substring(prev + 1, func.start));
                tmpLine.append(function(func.functionName, func.index, propertyRow++));
                prev = func.end;
                last = func.end;
            }
            tmpLine.append(line.substring(last + 1));
            tmpLine.append("\n");
            sb.append(tmpLine);
        }
        String context = sb.toString();

        System.out.println("最终转换过来的文件大小是：" + context.length());

        FileWriter fw = new FileWriter(new File(outputFile));
        fw.write(context);
        fw.flush();
        fw.close();

    }

    private static void getProperty() throws IOException {
        List<String> propertyLines = readText(propertyUrl);

        for (String line : propertyLines) {
            line = line.trim();
            if (line.equals(""))
                continue;

            Iterator<String> property = Splitter.on("\t")
                    .omitEmptyStrings().split(line)
                    .iterator();

            String index = property.next();
            String sentence = property.next();
            sentenceList.add(sentence);
            propertyMap.put(index, sentence);
        }
    }

    private static List<String> readText(String url) throws IOException {

        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setDoInput(true);
        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        List<String> propertyLines = Lists.newLinkedList();
        String line;
        while ((line = br.readLine()) != null) {
            propertyLines.add(line);
        }
        return propertyLines;
    }

    private static List<FunctionDesc> analysisFunctionsInLine(String line) {
        List<FunctionDesc> functionList = Lists.newArrayList();
        int signIndex, prev = -1;
        while ((signIndex = line.indexOf("$", prev + 1)) != -1) {

            int tmpEnd = line.indexOf("(", signIndex);
            String functionName = line.substring(signIndex + 1, tmpEnd);

            int end = line.indexOf(")", tmpEnd);
            String index = line.substring(tmpEnd + 1, end);
            functionList.add(new FunctionDesc(signIndex, end, functionName, index));
            prev = signIndex;
        }
        return functionList;
    }

    public static String function(String functionName, String index, int propertyRow) {
        boolean isDesc = false;

        if (functionName.equals("natureOrder")) {
            return sentenceList.get(propertyRow);
        } else if (functionName.equals("indexOrder")) {
            return propertyMap.get(index);
        } else if (functionName.equals("charOrderDESC")) {
            isDesc = true;
        }

        String[] sentenceChars = sentenceList.get(propertyRow).split("");
        if (isDesc) {
            Arrays.sort(sentenceChars);
        } else {
            Arrays.sort(sentenceChars, new Comparator<String>() {

                public int compare(String o1, String o2) {
                    return - o1.compareTo(o2);
                }

            });
        }
        StringBuilder charSortedSentence = new StringBuilder();
        for (String sentenceChar : sentenceChars) {
            charSortedSentence.append(sentenceChar);
        }
        return charSortedSentence.toString();
    }


    static class FunctionDesc {
        int start, end;
        String functionName;
        String index;

        FunctionDesc(int s, int e, String name, String index) {
            this.start = s;
            this.end = e;
            this.functionName = name;
            this.index = index;
        }

    }

}
