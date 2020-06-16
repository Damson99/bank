package bank.controller;

import bank.model.EmailBody;
import bank.model.SenderEvent;
import bank.model.Transfer;
import bank.model.User;
import bank.service.TransferService;
import bank.service.UserService;
import bank.utils.ActualDate;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfWriter;
import org.apache.pdfbox.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/history")
public class TransfersHistoryController
{
    @Autowired
    private MessageSource messageSource;

    @Autowired
    private ActualDate actualDate;

    @Autowired
    private TransferService transferService;

    @Autowired
    private UserService userService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;


    @GetMapping
    public ModelAndView main(WebRequest webRequest)
    {
        ModelAndView modelAndView = new ModelAndView();
        User user = userService.getUserWithAuthentication();
        modelAndView.addObject("user", user);
        modelAndView.addObject("date", actualDate.getTime());
        modelAndView.addObject("template", "history");
        modelAndView.addObject("pageTitle",
                messageSource.getMessage("history.page", null, webRequest.getLocale()));
        modelAndView.setViewName("main");
        modelAndView.addObject("transfers", getTransferHistory(user.getAccountNumber(), modelAndView,
                webRequest.getLocale()));
        modelAndView.addObject("emailBody", new EmailBody());

        user.setAccountNumber(userService.accountNumberForPage(user.getAccountNumber()));
        return modelAndView;
    }

    public List<Transfer> getTransferHistory(String accountNumber, ModelAndView modelAndView, Locale locale)
    {
        List<Transfer> transfers = transferService.findTransfers(accountNumber);

        if(transfers.isEmpty())
        {
            modelAndView.addObject("historyMessage",
                    messageSource.getMessage("nothing.to.show", null, locale));
        }
        else
        {
            for(Transfer tr : transfers)
            {
                tr.setRecipient(userService.accountNumberForPage(transferService.getRecipient(tr.getSenderRecipient())));
            }
        }

        return transfers;
    }

    @PostMapping("/print")
    public void downloadFile(Transfer transfer, HttpServletResponse response)
    {
        try
        {
            File createdFile = createFile(response, transfer);
            response.setHeader("Content-Disposition:", "attachment;filename=" + createdFile.getName());
            response.setContentType("application/pdf");

            InputStream in = new FileInputStream(createdFile);
            IOUtils.copy(in, response.getOutputStream());
            response.flushBuffer();
        }
        catch (IOException | DocumentException e)
        {
            e.printStackTrace();
            response.setStatus(500);
        }
    }

    @PostMapping("/{transfer}")
    public ModelAndView sendMail(@Valid EmailBody emailBody, BindingResult bindingResult, @PathVariable("transfer")
            Transfer transfer, HttpServletResponse response,
            RedirectAttributes redirectAttributes)
    {
        ModelAndView modelAndView = new ModelAndView();
        Locale locale = response.getLocale();
        modelAndView.setViewName("redirect:/history?lang=" + locale.getLanguage());

        return sendMailFunction(emailBody, bindingResult, transfer, response, redirectAttributes,
                modelAndView, locale);
    }

    @PostMapping("/{transfer}/{whichPage}")
    public ModelAndView sendMail2(@Valid EmailBody emailBody, BindingResult bindingResult, @PathVariable("transfer")
            Transfer transfer, @PathVariable("whichPage") Long whichPage, HttpServletResponse response,
                                  RedirectAttributes redirectAttributes)
    {
        ModelAndView modelAndView = new ModelAndView();
        Locale locale = response.getLocale();
        modelAndView.setViewName("redirect:/users/userId?userId=" + whichPage);

        return sendMailFunction(emailBody, bindingResult, transfer, response, redirectAttributes,
                modelAndView, locale);
    }

    private ModelAndView sendMailFunction(EmailBody emailBody, BindingResult bindingResult, Transfer transfer,
            HttpServletResponse response, RedirectAttributes redirectAttributes, ModelAndView modelAndView, Locale locale)
    {
        if(emailBody.getRecipient() == null || emailBody.getRecipient().equals(""))
        {
            redirectAttributes.addFlashAttribute("fatalError",
                    messageSource.getMessage("history.recipient.missing", null, locale));
            return modelAndView;
        }

        try
        {
            File createdFile = createFile(response, transfer);
            emailBody.setFile(createdFile);
        }
        catch (IOException | DocumentException ex)
        {
            ex.printStackTrace();
            redirectAttributes.addFlashAttribute("fatalError",
                    messageSource.getMessage("history.file", null, locale));
            return modelAndView;
        }

        if(emailBody.getFile() == null || !emailBody.getFile().exists())
        {
            redirectAttributes.addFlashAttribute("fatalError",
                    messageSource.getMessage("history.file", null, locale));
            return modelAndView;
        }

        if(bindingResult.hasErrors())
        {
            List<String> objectErrors = new ArrayList<>();
            for(Object object : bindingResult.getAllErrors())
            {
                if(object instanceof ObjectError)
                {
                    objectErrors.add(((ObjectError) object).getDefaultMessage());
                }
            }

            redirectAttributes.addFlashAttribute("fatalErrors", objectErrors);
            return modelAndView;
        }
        else
        {
            try
            {
                eventPublisher.publishEvent(new SenderEvent(locale, emailBody));
            }
            catch (Exception ex)
            {
                redirectAttributes.addFlashAttribute("fatalError",
                        messageSource.getMessage("error.page", null, locale));
                return modelAndView;
            }
        }

        redirectAttributes.addFlashAttribute("message",
                messageSource.getMessage("transfer.defined.success", null, locale));
        return modelAndView;
    }

    private File createFile(HttpServletResponse response, Transfer transfer) throws IOException, DocumentException
    {
        Locale locale = response.getLocale();
        User user = userService.getUserWithAuthentication();
        String sender = userService.accountNumberForPage(transferService.getSender(transfer.getSenderRecipient()));
        transfer.setRecipient(userService.accountNumberForPage(transferService.getRecipient(transfer.getSenderRecipient())));
        Document document = new Document(PageSize.A4);

        File file = File.createTempFile("transfer" + transfer.getId(),".pdf");
        PdfWriter.getInstance(document, new FileOutputStream(file));
        Font font = FontFactory.getFont(FontFactory.HELVETICA, 14);

        document.open();
        addChunk(document, msgSource("history.id", locale), transfer.getId().toString(), font);
        addChunk(document, msgSource("history.sender", locale), sender, font);
        if(transfer.getSenderBalance() != null)
        {
            addChunk(document, msgSource("transfer.amount", locale), "- " + transfer.getAmount(), font);
            addChunk(document, msgSource("history.balance", locale), transfer.getSenderBalance().toString(), font);
        }
        addChunk(document, msgSource("history.recipient", locale), transfer.getRecipient(), font);
        if(transfer.getRecipientBalance() != null)
        {
            addChunk(document, msgSource("transfer.amount", locale), "+ " + transfer.getAmount(), font);
            addChunk(document, msgSource("history.balance", locale), transfer.getRecipientBalance().toString(), font);
        }
        addChunk(document, msgSource("transfer.date", locale), transfer.getDate(), font);
        addChunk(document, msgSource("transfer.subject", locale), transfer.getSubject(), font);
        addChunk(document, msgSource("history.executed", locale), Boolean.toString(transfer.isExecuted()), font);
        document.close();

        PdfReader pdfReader = new PdfReader(file.getAbsolutePath());
        PdfStamper pdfStamper = new PdfStamper(pdfReader, new FileOutputStream("encryptedTransfer.pdf"));
        pdfStamper.setEncryption(user.getPersonalIdNumber().getBytes(), user.getPersonalIdNumber().getBytes(),
                0, PdfWriter.ENCRYPTION_AES_256);
        return file
    }

    private String msgSource(String msg, Locale locale)
    {
        return messageSource.getMessage(msg, null, locale);
    }

    private void addChunk(Document document, String key, String prop, Font font) throws DocumentException
    {
        Chunk chunk = new Chunk(key + " : " + prop, font);
        document.add(chunk);
        document.add(new Paragraph("\n"));
    }
}
