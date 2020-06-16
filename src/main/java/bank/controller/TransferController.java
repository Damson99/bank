package bank.controller;

import bank.model.*;
import bank.service.*;
import bank.utils.ActualDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@RestController
@RequestMapping("/transfer")
public class TransferController
{
    @Autowired
    private VerificationTokenService verificationTokenService;

    @Autowired
    private UserService userService;

    @Autowired
    private BankFundsService bankFundsService;

    @Autowired
    private TransferService transferService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private ActualDate actualDate;

    @Autowired
    private PasswordVerificationTokenService passwordVerificationTokenService;

    @Value("${transfer.tax}")
    private BigDecimal BANK_TAX;

    @Value("${payment.confirmation.link}")
    private String paymentConfirmationLink;


    @GetMapping()
    public ModelAndView main(WebRequest webRequest)
    {
        ModelAndView modelAndView = new ModelAndView();
        User user = userService.getUserWithAuthentication();
        user.setAccountNumber(userService.accountNumberForPage(user.getAccountNumber()));
        List<DefinedTransfer> definedTransfers = transferService.findDefinedTransfers(user.getId());

        if(!definedTransfers.isEmpty())
        {
            for(DefinedTransfer definedTransfer : definedTransfers)
            {
                definedTransfer.setRecipient(userService.accountNumberForPage(definedTransfer.getRecipient()));
            }
        }

        modelAndView.addObject("user", user);
        modelAndView.addObject("date", actualDate.getTime());
        modelAndView.addObject("transfer", new Transfer());
        modelAndView.addObject("definedTransfer", new DefinedTransfer());
        modelAndView.addObject("definedTransfers", definedTransfers);
        modelAndView.addObject("transferNumberForm", 0);
        modelAndView.addObject("template", "transfer");
        modelAndView.addObject("transferForm", true);
        modelAndView.addObject("definedNewTransferForm", false);
        modelAndView.addObject("definedTransferForm", true);
        modelAndView.addObject("pageTitle", messageSource.getMessage("transfer.page", null, webRequest.getLocale()));
        modelAndView.setViewName("main");
        return modelAndView;
    }

    @GetMapping("/paymentConfirm")
    public ModelAndView paymentConfirm(WebRequest webRequest, @RequestParam("token") String token, RedirectAttributes redirectAttributes)
    {
        ModelAndView modelAndView = new ModelAndView();
        Locale locale = webRequest.getLocale();
        modelAndView.setViewName("redirect:/transfer?lang=" + locale.getLanguage());

        VerificationToken verificationToken = verificationTokenService.getVerificationToken(token);
        Lock bankLock = new ReentrantLock();

        if(verificationToken == null)
        {
            redirectAttributes.addFlashAttribute("fatalError",
                    messageSource.getMessage("auth.message.invalidToken", null, locale));
            return modelAndView;
        }

        if(passwordVerificationTokenService.expiryDate(verificationToken.getExpiryDate()))
        {
            redirectAttributes.addFlashAttribute("fatalError",
                    messageSource.getMessage("auth.message.expired", null, locale));
            return modelAndView;
        }

        Transfer transfer = verificationToken.getTransfer();
        if(transfer.isExecuted())
        {
            redirectAttributes.addFlashAttribute("fatalError",
                    messageSource.getMessage("transfer.used", null, locale));
            return modelAndView;
        }

        User senderDTO = null;
        User recipientDTO = null;
        String sender = transferService.getSender(transfer.getSenderRecipient());
        String recipient = transferService.getRecipient(transfer.getSenderRecipient());
        transfer.setRecipient(recipient);

        try
        {
            senderDTO = userService.findByAccountNumber(sender);
        }
        catch (NullPointerException e)
        {
            redirectAttributes.addFlashAttribute("fatalError",
                    messageSource.getMessage("transfer.sender.does.not.exist", null, locale));
            return modelAndView;
        }
        try
        {
            recipientDTO = userService.findByAccountNumber(recipient);
        }
        catch (NullPointerException e)
        {
            redirectAttributes.addFlashAttribute("fatalError",
                    messageSource.getMessage("transfer.recipient.does.not.exist", null, locale));
            return modelAndView;
        }


        if(!senderDTO.isEnabled())
        {
            redirectAttributes.addFlashAttribute("fatalError",
                    messageSource.getMessage("transfer.me.disabled", null, locale));
            modelAndView.setViewName("redirect:/logout?lang=" + locale.getLanguage());
            return modelAndView;
        }
        else if(!recipientDTO.isEnabled())
        {
            redirectAttributes.addFlashAttribute("fatalError",
                    messageSource.getMessage("transfer.user.disabled", null, locale));
            return modelAndView;
        }
        else if(transfer.getRecipient().equals("")
                || userService.findByAccountNumber(transfer.getRecipient()) == null)
        {
            redirectAttributes.addFlashAttribute("fatalError",
                    messageSource.getMessage("transfer.accountNumber.not.exist", null, locale));
            return modelAndView;
        }

        bankLock.lock();
        try
        {
            redirectAttributes.addFlashAttribute(makeTransfer(transfer, locale, senderDTO, recipientDTO, redirectAttributes));
        }
        finally
        {
            bankLock.unlock();
        }

        try
        {
            String subjectSender = messageSource.getMessage("transfer.incoming.subject", null, locale);
            String messageSender = "<p>- " + transfer.getAmount() + " PLN" + "</p>"
                    + "<p>" + messageSource.getMessage("transfer.done", null, locale) + "</p>"
                    + "<p>" + messageSource.getMessage("currency.date", null, locale) + " " + transfer.getDate() + "</p>"
                    + "<p>" + messageSource.getMessage("available.funds", null, locale) + " " + senderDTO.getFunds() + "</p>";

            String subjectRecipient = messageSource.getMessage("transfer.incoming.subject", null, locale);
            String messageRecipient = "<p>+ " + transfer.getAmount() + " PLN" + "</p>"
                    + "<p>" + messageSource.getMessage("transfer.incoming", null, locale) + "</p>"
                    + "<p>" + messageSource.getMessage("currency.date", null, locale) + " " + transfer.getDate() + "</p>"
                    + "<p>" + messageSource.getMessage("available.funds", null, locale) + " " + recipientDTO.getFunds() + "</p>";

            EmailBody emailBodySender = new EmailBody(subjectSender, messageSender, senderDTO.getEmail());
            eventPublisher.publishEvent(new SenderEvent(locale, emailBodySender));

            EmailBody emailBodyRecipient = new EmailBody(subjectRecipient, messageRecipient, recipientDTO.getEmail());
            eventPublisher.publishEvent(new SenderEvent(locale, emailBodyRecipient));
        }
        catch (Exception ex)
        {
        }
        return modelAndView;
    }

    private RedirectAttributes makeTransfer(Transfer transfer, Locale locale, User senderDTO, User recipientDTO,
                                            RedirectAttributes redirectAttributes)
    {
        if(transfer.getAmount().compareTo(BigDecimal.ZERO) == 0)
        {
            redirectAttributes.addFlashAttribute("fatalError",
                    messageSource.getMessage("transfer.small.amount", null, locale));
        }

        BigDecimal senderFundsBeforeTax = senderDTO.getFunds().subtract(transfer.getAmount());
        BigDecimal senderFinalFunds = senderFundsBeforeTax.subtract(BANK_TAX);
        BankFunds addTaxWithBankFunds = bankFundsService.getBankFunds();
        addTaxWithBankFunds.setBankFunds(addTaxWithBankFunds.getBankFunds().add(BANK_TAX));
        BigDecimal recipientFinalFunds = recipientDTO.getFunds().add(transfer.getAmount());

        if(senderFinalFunds.compareTo(BigDecimal.ZERO) <= 0)
        {
            return redirectAttributes.addFlashAttribute("fatalError",
                    messageSource.getMessage("transfer.small.funds", null, locale));
        }
        else
        {
            try
            {
                transfer.setExecuted(true);
                senderDTO.setFunds(senderFinalFunds);
                recipientDTO.setFunds(recipientFinalFunds);

                transferService.setTransferExecuted(transfer.isExecuted(), transfer.getId());
                userService.updateUserFunds(senderFinalFunds, senderDTO.getAccountNumber());
                userService.updateUserFunds(recipientFinalFunds, transfer.getRecipient());
                bankFundsService.updateBankFunds(addTaxWithBankFunds);
            }
            catch(Exception e)
            {
                e.printStackTrace();
                return redirectAttributes.addFlashAttribute("fatalError",
                        messageSource.getMessage("transfer.database.error", null, locale));
            }
        }

        return redirectAttributes.addFlashAttribute("message",
                messageSource.getMessage("transfer.success", null, locale));
    }

    @GetMapping("/defineNew")
    public ModelAndView defineNew(WebRequest webRequest)
    {
        ModelAndView modelAndView = new ModelAndView();
        User user = userService.getUserWithAuthentication();
        user.setAccountNumber(userService.accountNumberForPage(user.getAccountNumber()));

        modelAndView.addObject("transferForm", false);
        modelAndView.addObject("definedNewTransferForm", true);
        modelAndView.addObject("definedTransferForm", false);
        modelAndView.addObject("definedTransfer", new DefinedTransfer());
        modelAndView.addObject("user", user);
        modelAndView.addObject("date", actualDate.getTime());
        modelAndView.addObject("template", "transfer");
        modelAndView.addObject("pageTitle", messageSource.getMessage("transfer.page", null, webRequest.getLocale()));
        modelAndView.setViewName("main");
        return modelAndView;
    }

    @PostMapping("{isTransferDefined}")
    public ModelAndView transfer(@Valid Transfer transfer, BindingResult bindingResult, HttpServletRequest request,
                                 @PathVariable("isTransferDefined") int transferNumberForm, RedirectAttributes redirectAttributes)
    {
        ModelAndView modelAndView = new ModelAndView();
        Locale locale = request.getLocale();
        String time = actualDate.getTime();
        User user = userService.getUserWithAuthentication();
        modelAndView.addObject("transferForm", false);
        modelAndView.addObject("definedNewTransferForm", false);
        modelAndView.addObject("definedTransferForm", false);
        modelAndView.addObject("user", user);
        modelAndView.addObject("date", time);
        modelAndView.addObject("template", "transfer");
        modelAndView.addObject("pageTitle", messageSource.getMessage("transfer.page", null, locale));
        modelAndView.setViewName("main");

        if(transferNumberForm > 0)
        {
            List<DefinedTransfer> definedTransfers = transferService.findDefinedTransfers(user.getId());

            modelAndView.addObject("definedTransfers", definedTransfers);
            modelAndView.addObject("definedTransferForm", true);
            modelAndView.addObject("transferNumberForm", transferNumberForm);
        }
        else
        {
            modelAndView.addObject("transferNumberForm", 0);
            modelAndView.addObject("transferForm", true);
        }

        if(transfer.getRecipient().trim().length() != 29)
        {
            user.setAccountNumber(userService.accountNumberForPage(user.getAccountNumber()));
            modelAndView.addObject("fatalError",
                    messageSource.getMessage("accountNumber.size", null, locale));
            return modelAndView;
        }

        if(!transfer.getRecipient().equals("") || transfer.getRecipient() != null)
        {
            transfer.setRecipient(userService.accountNumberForDB(transfer.getRecipient()));
            User recipient = userService.findByAccountNumber(transfer.getRecipient());

            if(recipient == null)
            {
                bindingResult.rejectValue("recipient", "error.transfer",
                        messageSource.getMessage("transfer.recipient.does.not.exist", null, locale));
            }
            else
            {
                if(!recipient.isEnabled())
                {
                    bindingResult.rejectValue("recipient", "error.transfer",
                            messageSource.getMessage("transfer.user.disabled", null, locale));
                }
                if(recipient.getFunds() == null)
                {
                    bindingResult.rejectValue("amount", "error.transfer",
                            messageSource.getMessage("error.transfer.recipient.balance.null", null, locale));
                }
                else
                {
                    transfer.setRecipientBalance(recipient.getFunds());
                }
            }
        }
        else
        {
            bindingResult.rejectValue("recipient", "error.transfer",
                    messageSource.getMessage("transfer.accountNumber.not.exist", null, locale));
        }

        if(user.getAccountNumber().equals(transfer.getRecipient()))
        {
            bindingResult.rejectValue("recipient", "error.transfer",
                    messageSource.getMessage("its.your.accountNumber", null, locale));
        }

        if(user.getFunds() != null && transfer.getAmount() != null
                && user.getFunds().subtract(transfer.getAmount().add(BANK_TAX)).compareTo(BigDecimal.ZERO) <= 0)
        {
            bindingResult.rejectValue("amount", "error.transfer",
                    messageSource.getMessage("transfer.small.funds", null, locale));
        }

        transfer.setSenderRecipient(transferService.getRelationSenderRecipient(user.getAccountNumber(), transfer.getRecipient()));
        transfer.setSenderBalance(user.getFunds());
        transfer.setDate(time);

        if(bindingResult.hasErrors())
        {
            transfer.setRecipient(userService.accountNumberForPage(transfer.getRecipient()));
            user.setAccountNumber(userService.accountNumberForPage(user.getAccountNumber()));
            return modelAndView;
        }
        else
        {
            try
            {
                String token = UUID.randomUUID().toString();
                transferService.saveTransfer(transfer);
                verificationTokenService.createVerificationToken(transfer, token);

                String subject = messageSource.getMessage("payment.confirmation.subject", null, locale);
                String message = "<a href=\"http://" + new URL(request.getRequestURL().toString()).getHost() + paymentConfirmationLink + token + "\">"
                        + messageSource.getMessage("payment.confirmation.link.inscription", null, locale) + "</a>";

                EmailBody emailBody = new EmailBody(subject, message, user.getEmail());
                eventPublisher.publishEvent(new SenderEvent(locale, emailBody));
            }
            catch (Exception ex)
            {
                modelAndView.addObject("fatalError",
                        messageSource.getMessage("error.transfer.link", null, locale));
                user.setAccountNumber(userService.accountNumberForPage(transfer.getRecipient()));
                transfer.setRecipient(userService.accountNumberForPage(transfer.getRecipient()));
                return modelAndView;
            }

            redirectAttributes.addFlashAttribute("message", messageSource.getMessage("transfer.verification", null, locale));
            return new ModelAndView("redirect:/transfer?lang=" + locale.getLanguage());
        }
    }

    @PostMapping("/defineNew")
    public ModelAndView defineNew(@Valid DefinedTransfer definedTransfer, BindingResult bindingResult, WebRequest webRequest,
                                  RedirectAttributes redirectAttributes)
    {
        ModelAndView modelAndView = new ModelAndView();
        Locale locale = webRequest.getLocale();
        User user = userService.getUserWithAuthentication();
        modelAndView.addObject("user", user);
        modelAndView.addObject("date", actualDate.getTime());
        modelAndView.addObject("template", "transfer");
        modelAndView.addObject("transferForm", false);
        modelAndView.addObject("definedNewTransferForm", true);
        modelAndView.addObject("definedTransferForm", false);
        modelAndView.addObject("pageTitle", messageSource.getMessage("transfer.page", null, locale));
        modelAndView.setViewName("main");
        definedTransfer.setUserId(user.getId());

        if(definedTransfer.getRecipient().trim().length() != 29)
        {
            modelAndView.addObject("fatalError",
                    messageSource.getMessage("accountNumber.size", null, locale));
            return modelAndView;
        }

        definedTransfer.setRecipient(userService.accountNumberForDB(definedTransfer.getRecipient()));
        User definedTransferExist = transferService.findByRecipient(definedTransfer.getRecipient());
        User recipient = userService.findByAccountNumber(definedTransfer.getRecipient());

        if(definedTransferExist != null)
        {
            bindingResult.rejectValue("recipient", "error.definedTransfer",
                    messageSource.getMessage("transfer.accountNumber.already.defined", null, locale));
        }

        if(definedTransfer.getRecipient().equals("") || recipient == null)
        {
            bindingResult.rejectValue("recipient", "error.definedTransfer",
                    messageSource.getMessage("transfer.accountNumber.not.exist", null, locale));
        }
        else if(user.getAccountNumber().equals(definedTransfer.getRecipient()))
        {
            bindingResult.rejectValue("recipient", "error.definedTransfer",
                    messageSource.getMessage("its.your.accountNumber", null, locale));
        }

        if(bindingResult.hasErrors())
        {
            definedTransfer.setRecipient(userService.accountNumberForPage(definedTransfer.getRecipient()));
        }
        else
        {
            transferService.saveDefinedTransfer(definedTransfer);
            redirectAttributes.addFlashAttribute("message",
                    messageSource.getMessage("transfer.defined.success", null, locale));
            modelAndView.setViewName("redirect:/transfer?lang=" + locale.getLanguage());
        }

        return modelAndView;
    }

    @PostMapping("/defineNew/delete")
    public ModelAndView deleteDefinedTransfer(@RequestParam("definedTransferId") Long definedTransferId,
                                              RedirectAttributes redirectAttributes, WebRequest webRequest)
    {
        redirectAttributes.addFlashAttribute("message",
                messageSource.getMessage("definedTransfer.deleted", null, webRequest.getLocale()));
        transferService.deleteDefinedTransfer(definedTransferId);
        return new ModelAndView("redirect:/transfer?lang=" + webRequest.getLocale().getLanguage());
    }

}
