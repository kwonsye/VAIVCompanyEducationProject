package auto_complete.util;

import java.util.stream.Stream;

public class StringUtil {
    public static String convertToNoSpacing(String originalText){
        return originalText.replace(" ", "");
    }

    public static String[] splitBracketWithNoSpacing(String originalText){
        String[] splits = originalText.split("\\(", 2);

        splits[0] = convertToNoSpacing(splits[0]);

        if(splits.length == 2){
            splits[1] = convertToNoSpacing(splits[1]);
        }
        return splits;
    }
}
