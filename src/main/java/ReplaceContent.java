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
    private static final String outputFile = "/Users/kingwufeng/workspace/ReplaceContent/sdxl.txt";

    private static LinkedList<String> indexList = Lists.newLinkedList();
    private static HashMap<String, String> propertyMap = Maps.newHashMap();

    public static void main(String[] args) {

        try {
            replaceText();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static List<String> readText(String url) throws IOException {

        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setDoInput(true);
        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        List<String> propertyLines = Lists.newLinkedList();
        String s;
        while ((s = br.readLine()) != null) {
            propertyLines.add(s);
        }
        return propertyLines;
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
            indexList.add(sentence);
            propertyMap.put(index, sentence);
        }
    }

    private static List<FunctionDesc> analysisFunctionsInLine(String line) {
        List<FunctionDesc> funtionList = Lists.newArrayList();
        int signIndex, prev = -1;
        while ((signIndex = line.indexOf("$", prev + 1)) != -1) {

            int tmpEnd = line.indexOf("(", signIndex);
            String functionName = line.substring(signIndex + 1, tmpEnd);

            int end = line.indexOf(")", tmpEnd);
            String index = line.substring(tmpEnd + 1, end);
            funtionList.add(new FunctionDesc(signIndex, end, functionName, index));
            prev = signIndex;
        }
        return funtionList;
    }

    private static String function(String functionName, String indexName, int i) {
        boolean desc = false;
        if (functionName.equals("natureOrder")) {
            return indexList.get(i);
        } else if (functionName.equals("indexOrder")) {
            return propertyMap.get(indexName);
        } else if (functionName.equals("charOrderDESC")) {
            desc = true;
        }
        String[] items = indexList.get(i).split("");
        if (desc) {
            Arrays.sort(items);
        } else {

            Arrays.sort(items, new Comparator<String>() {

                public int compare(String o1, String o2) {
                    return -o1.compareTo(o2);
                }

            });
        }
        StringBuilder buffer = new StringBuilder();
        for (String item : items) {
            buffer.append(item);
        }
        return buffer.toString();
    }

    private static void replaceText() throws IOException {
        getProperty();
        List<String> articleLines = readText(articleUrl);
        StringBuilder sb = new StringBuilder();
        int index = 0;
        for (String line : articleLines) {
            line = line.trim();
            if (line.equals("")) {
                sb.append("\n");
                continue;
            }
            List<FunctionDesc> items = analysisFunctionsInLine(line);
            int last = 0;
            StringBuilder tmpLine = new StringBuilder();
            int prev = -1;
            for (FunctionDesc fun : items) {
                tmpLine.append(line.substring(prev + 1, fun.start));
                tmpLine.append(function(fun.functionName, fun.index, index++));
                prev = fun.end;
                last = fun.end;
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

        public String toString() {
            return "[start = " + start + ", end = " + end + ", functionName = " + functionName + ", index = " + index + "]";
        }
    }

}
