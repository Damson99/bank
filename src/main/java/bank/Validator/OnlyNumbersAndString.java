package bank.Validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OnlyNumbersAndString implements ConstraintValidator<ValidOnlyNumbersAndString, String>
{
    @Override
    public void initialize(ValidOnlyNumbersAndString validOnlyNumbersAndString)
    {}

    @Override
    public boolean isValid(String string, ConstraintValidatorContext context)
    {
        return stringAndNumbersIllegalCharacters(string);
    }

    private boolean stringAndNumbersIllegalCharacters(String chars)
    {
        Pattern pattern = Pattern.compile("[~!@#$%^&*()_=+.,;:{}<>\\[\\]|\"\\-]\"]");
        Matcher matcher = pattern.matcher(chars);
        if(matcher.find()) return false;
        return true;
    }
}
