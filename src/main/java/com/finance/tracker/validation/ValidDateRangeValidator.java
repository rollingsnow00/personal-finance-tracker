package com.finance.tracker.validation;

import com.finance.tracker.dto.request.TransactionSearchRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidDateRangeValidator implements ConstraintValidator<ValidDateRange, TransactionSearchRequest> {
    @Override
    public boolean isValid(TransactionSearchRequest value, ConstraintValidatorContext context) {
        if (value == null || value.from() == null || value.to() == null) return true;
        if (!value.from().isAfter(value.to())) return true;
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                .addPropertyNode("from").addConstraintViolation();
        return false;
    }
}
