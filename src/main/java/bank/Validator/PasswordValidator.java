package bank.Validator;

import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class PasswordValidator
{
    public boolean passwordIllegalCharacters(String chars)
    {
        Pattern pattern = Pattern.compile("[~#;$&!:@*+%{}<>\\[\\]|\"^]");
        Matcher matcher = pattern.matcher(chars);
        if(matcher.find()) return true;
        return false;
    }
}
