package bank.Validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OnlyString implements ConstraintValidator<ValidOnlyString, String>
{
    @Override
    public void initialize(ValidOnlyString validOnlyString)
    {}

    @Override
    public boolean isValid(String string, ConstraintValidatorContext context)
    {
        return stringIllegalCharacters(string);
    }

    private boolean stringIllegalCharacters(String chars)
    {
        Pattern pattern = Pattern.compile("[0-9~!@#$%^&*()_=+.,;:{}<>\\[\\]|\"\\-]");
        Matcher matcher = pattern.matcher(chars);
        if(matcher.find()) return false;
        return true;
    }
}
