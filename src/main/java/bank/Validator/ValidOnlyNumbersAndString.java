package bank.Validator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = OnlyNumbersAndString.class)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidOnlyNumbersAndString
{
    String message() default "*Illegal characters.";

    Class<?> [] groups() default {};

    Class<? extends Payload> [] payload() default {};

}
