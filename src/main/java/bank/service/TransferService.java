package bank.service;

import bank.model.DefinedTransfer;
import bank.model.Transfer;
import bank.model.User;
import bank.repository.DefinedTransferRepository;
import bank.repository.TransferRepository;
import bank.utils.ActualDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransferService
{
    @Autowired
    private TransferRepository transferRepository;

    @Autowired
    private DefinedTransferRepository definedTransferRepository;

    @Autowired
    private ActualDate actualDate;


    public void saveTransfer(Transfer transfer)
    {
        transfer.setExecuted(false);
        transfer.setDate(actualDate.getTime());
        transferRepository.save(transfer);
    }

    public void setTransferExecuted(boolean executed, Long id)
    {
        transferRepository.setTransferExecuted(executed, id);
    }

    public List<Transfer> findTransfers(String accountNumber)
    {
        return transferRepository.findBySenderRecipientContainsAndExecutedTrueOrderByIdDesc(accountNumber);
    }

    public String getRelationSenderRecipient(String sender, String recipient)
    {
        return sender + ":" + recipient;
    }

    public String getSender(String senderRecipient)
    {
        return senderRecipient.split(":")[0];
    }

    public String getRecipient(String senderRecipient)
    {
        return senderRecipient.split(":")[1];
    }


    public void saveDefinedTransfer(DefinedTransfer definedTransfer)
    {
        definedTransferRepository.save(definedTransfer);
    }

    public List<DefinedTransfer> findDefinedTransfers(Long userId)
    {
        return definedTransferRepository.findByUserId(userId);
    }

    public User findByRecipient(String recipient)
    {
        return definedTransferRepository.findByRecipient(recipient);
    }

    public void deleteDefinedTransfer(Long definedTransferId)
    {
        definedTransferRepository.deleteById(definedTransferId);
    }
}
