package bank.Validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OnlyNumbers implements ConstraintValidator<ValidOnlyNumbers, String>
{
    @Override
    public void initialize(ValidOnlyNumbers validOnlyNumbers)
    {}

    @Override
    public boolean isValid(String string, ConstraintValidatorContext context)
    {
        return numbersIllegalCharacters(string);
    }

    private boolean numbersIllegalCharacters(String chars)
    {
        Pattern pattern = Pattern.compile("[A-Za-z~!@#$%^&*()_=+.,;:{}<>\\[\\]|\"\\-]");
        Matcher matcher = pattern.matcher(chars);
        if(matcher.find()) return false;
        return true;
    }
}
