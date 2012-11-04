import java.text.AttributedCharacterIterator;
import java.text.CharacterIterator;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Lars Vandenbergh
 */
public class DateFormatter {
    public static void main(String[] args) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("m:ss.00");
        AttributedCharacterIterator iterator = simpleDateFormat.formatToCharacterIterator(new Date());
        char next = iterator.first();
        while (next != CharacterIterator.DONE) {
            System.out.println(next);
            next = iterator.next();
        }

    }
}
