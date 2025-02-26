package com.codegym.controller.bill;

import com.codegym.model.DTO.IBillDTo;
import com.codegym.model.account.Account;
import com.codegym.model.receipt.*;
import com.codegym.model.user.Checker;
import com.codegym.model.user.Staff;
import com.codegym.service.account.IAccountService;
import com.codegym.service.checker.ICheckerService;
import com.codegym.service.email.EmailService;
import com.codegym.model.email.MailObject;
import com.codegym.service.bill.IBillService;
import com.codegym.service.billOption.IBillOptionService;
import com.codegym.service.billstatus.IBillStatusService;
import com.codegym.service.staff.IStaffService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.time.temporal.ChronoUnit.HOURS;

@Controller
@CrossOrigin("*")
@RequestMapping("/api/bills")
public class RestBillController {

    @Autowired
    private IBillService billService;

    @Autowired
    private IAccountService accountService;

    @Autowired
    private IBillOptionService billOptionService;

    @Autowired
    private IBillStatusService billStatusService;

    @Autowired
    private IStaffService staffService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private ICheckerService checkerService;

    @GetMapping
    public ResponseEntity<Iterable<BillDTO>> getAll() {
//        MailObject mailObject = new MailObject("hoangbaoanhng18@gmail.com", "hieudaohn94@gmail.com", "sign in successful", "congratulation Hieu to is first member in tinder windy club, click to: http://localhost:8080/ to dating with KAX ");
//        emailService.sendSimpleMessage(mailObject);
        Iterable<Bill> bills = billService.findAll();
        List<BillDTO> billDTOList = new ArrayList<>();
        for (Bill b: bills
             ) {
            billDTOList.add(new BillDTO(b.getId(), b.getDateOrder(), b.getDateEnd(), b.getAmount(), b.getStaff().getAccount().getUsername(), b.getChecker().getAccount().getUsername(), b.getBillStatus().getName()));
        }
        Iterable<BillDTO> billDTOS = billDTOList;
        return new ResponseEntity<>(billDTOS, HttpStatus.OK);
    }

    @GetMapping("/")
    public ResponseEntity<Page<Bill>> showAll(@PageableDefault(value = 4) Pageable pageable) {

        return new ResponseEntity<>(billService.findAll(pageable), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Bill> findBillById(@PathVariable Long id) {
        Optional<Bill> billOptional = billService.findById(id);
        if (!billOptional.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(billOptional.get(), HttpStatus.OK);
    }


    @PutMapping("/{id}")
    public ResponseEntity<Bill> updateBillById(@PathVariable Long id, @RequestBody Bill bill) {
        Optional<Bill> billOptional = billService.findById(id);
        billOptional.get().setBillStatus(bill.getBillStatus());
        billService.save(billOptional.get());
        return new ResponseEntity<>(billOptional.get(), HttpStatus.OK);
    }

    @PostMapping
    @ResponseBody
    public ResponseEntity<Bill> saveBill(@RequestBody Bill bill) {

        BillStatus billStatus = billStatusService.findById(1L).get();
        bill.setBillStatus(billStatus);

//        Account accountChecker = bill.getChecker().getAccount();
//        double amountChecker = accountChecker.getBalance() - bill.getAmount();
//        accountChecker.setBalance(amountChecker);
//        accountService.save(accountChecker);
//
//        Optional<Account> accountAdmin = accountService.findById(1L);
//        double amountAdmin = accountAdmin.get().getBalance() + bill.getAmount();
//        accountAdmin.get().setBalance(amountAdmin);
//        accountService.save(accountAdmin.get());
        return new ResponseEntity<>(billService.save(bill), HttpStatus.CREATED);
    }

    @PostMapping("/")
    @ResponseBody
    public ResponseEntity<Bill> saveBillDTO(@RequestBody Bill bill) {
        Account accountChecker = bill.getChecker().getAccount();
        double amountChecker = accountChecker.getBalance() - bill.getAmount();
        accountChecker.setBalance(amountChecker);
        accountService.save(accountChecker);

        Optional<Account> accountAdmin = accountService.findById(1L);
        double amountAdmin = accountAdmin.get().getBalance() + bill.getAmount();
        accountAdmin.get().setBalance(amountAdmin);
        accountService.save(accountAdmin.get());
        return new ResponseEntity<>(billService.save(bill), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Bill> deleteBill(@PathVariable Long id) {
        Optional<Bill> billOptional = billService.findById(id);
        if (!billOptional.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            billService.remove(id);
        }
        return new ResponseEntity<>(billOptional.get(), HttpStatus.OK);
    }

    @GetMapping("/hour/{id}")
    public ResponseEntity<Integer> getHoursDifferent(@PathVariable Long id) {
        Bill bill = billService.findById(id).get();
        Integer time = Integer.parseInt(String.valueOf(HOURS.between(bill.getDateOrder(),bill.getDateEnd())));
        return new ResponseEntity<>(time, HttpStatus.OK);
    }

    @PutMapping("/amount/{id}/{amount}")
    public ResponseEntity<Bill> setAmount(@PathVariable Long id, @PathVariable double amount) {
        Bill bill = billService.findById(id).get();
        bill.setAmount(amount);
        billService.save(bill);
        return new ResponseEntity<>(bill, HttpStatus.OK);
    }

    @GetMapping("/view/{id}")
    public ResponseEntity<BillDTO> showBill(@PathVariable Long id) {
        Optional<Bill> billOptional = billService.findById(id);
        if (!billOptional.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            Bill bill = billOptional.get();
            BillDTO billDTO = new BillDTO(bill.getId(), bill.getDateOrder(),  bill.getDateEnd(), bill.getAmount(),  bill.getStaff().getAccount().getUsername(), bill.getChecker().getAccount().getUsername(), bill.getBillStatus().getName());
            return new ResponseEntity<>(billDTO, HttpStatus.OK);
        }
    }


    @PutMapping("/editStatus")
    public ResponseEntity<Bill> editStatusBill(@RequestBody BillDTO billDTO) {
        Optional<Bill> billOptional = billService.findById(billDTO.getId());
        if (!billOptional.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            BillStatus billStatus = billStatusService.findBillStatusByName(billDTO.getBillStatusName());
            billOptional.get().setBillStatus(billStatus);
            Bill bill = billService.save(billOptional.get());
            return new ResponseEntity<>(bill, HttpStatus.OK);
        }
    }

    @PutMapping("/editStatus/{id}")
    public ResponseEntity<Bill> acceptStatusBill(@PathVariable Long id) {
        Optional<Bill> billOptional = billService.findById(id);
        if (billOptional.get().getBillStatus().getId() != 1) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            Long idStatus = 2L;
            BillStatus billStatus = billStatusService.findById(idStatus).get();
            billOptional.get().setBillStatus(billStatus);
            billService.save(billOptional.get());
            MailObject mailObject = new MailObject("noreply@tinderwindy.com", billOptional.get().getChecker().getAccount().getEmail(), "Your order is Verified", "Please check information your date: your partner: " + billOptional.get().getStaff().getName() + " national: " + billOptional.get().getStaff().getNationality() + ", height: " + billOptional.get().getStaff().getHeight() +", weight: " +billOptional.get().getStaff().getWeight() + ", total price: " + billOptional.get().getAmount());
            emailService.sendSimpleMessage(mailObject);
            return new ResponseEntity<>(billOptional.get(), HttpStatus.OK);
        }
    }
    @PutMapping("/cancelStatus/{id}")
    public ResponseEntity<Bill> cancelStatusBill(@PathVariable Long id) {
        Optional<Bill> billOptional = billService.findById(id);
        if (billOptional.get().getBillStatus().getId() != 1) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            Long idStatus = 5L;
            BillStatus billStatus = billStatusService.findById(idStatus).get();
            billOptional.get().setBillStatus(billStatus);
            billService.save(billOptional.get());
            //calculator money
            Account accountChecker = billOptional.get().getChecker().getAccount();
            double amountChecker = accountChecker.getBalance() + billOptional.get().getAmount();
            accountChecker.setBalance(amountChecker);
            accountService.save(accountChecker);

            Optional<Account> accountAdmin = accountService.findById(1L);
            double amountAdmin = accountAdmin.get().getBalance() - billOptional.get().getAmount();
            accountAdmin.get().setBalance(amountAdmin);
            accountService.save(accountAdmin.get());
            MailObject mailObject = new MailObject("noreply@tinderwindy.com", billOptional.get().getChecker().getAccount().getEmail(), "Your order has been Cancelled", billOptional.get().getStaff().getName() + " is busy. Please login and choose other staff. So sorry for this inconvenient and thank u so much!");
            emailService.sendSimpleMessage(mailObject);
            return new ResponseEntity<>(billOptional.get(), HttpStatus.OK);
        }
    }

    @PutMapping("/setStatusCompleted/{id}")
    public ResponseEntity<Bill> completedStatusBill(@PathVariable Long id) {
        Optional<Bill> billOptional = billService.findById(id);
        if (billOptional.get().getBillStatus().getId() != 6) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            Long idStatus = 4L;
            BillStatus billStatus = billStatusService.findById(idStatus).get();
            billOptional.get().setBillStatus(billStatus);
            billService.save(billOptional.get());
            // tính tiền
            double amount = billOptional.get().getAmount();
            double amountStaff = amount * 70/100;
            Optional<Staff> staff = staffService.findById(billOptional.get().getStaff().getId());
            // cần tìm 1 account có staff như trên
            Account account = staff.get().getAccount();
            double amountAccount = account.getBalance() + amountStaff;
            account.setBalance(amountAccount);
            Optional<Account> accountAdmin = accountService.findById(1L);
            double amountAdmin = accountAdmin.get().getBalance() - amountStaff;
            accountAdmin.get().setBalance(amountAdmin);
            accountService.save(accountAdmin.get());
            accountService.save(account);
            MailObject mailObject2 = new MailObject("noreply@tinderwindy.com", billOptional.get().getStaff().getAccount().getEmail(), "Your Date completed", "Thank you for your co-oparation! your payment request has been processed");
            emailService.sendSimpleMessage(mailObject2);
            return new ResponseEntity<>(billOptional.get(), HttpStatus.OK);
        }
    }

    @PutMapping("/setStatusProcessing/{id}")
    public ResponseEntity<Bill> processingStatusBill(@PathVariable Long id) {
        Optional<Bill> billOptional = billService.findById(id);
        if (billOptional.get().getBillStatus().getId() != 2) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            Long idStatus = 3L;
            BillStatus billStatus = billStatusService.findById(idStatus).get();
            billOptional.get().setBillStatus(billStatus);
            billService.save(billOptional.get());
            MailObject mailObject1 = new MailObject("noreply@tinderwindy.com", billOptional.get().getChecker().getAccount().getEmail(), "Your Date completed", "Thank for use our service! Best wish for u");
            emailService.sendSimpleMessage(mailObject1);
            MailObject mailObject2 = new MailObject("noreply@tinderwindy.com", billOptional.get().getStaff().getAccount().getEmail(), "Your Date completed", "Thank you for your co-oparation!");
            emailService.sendSimpleMessage(mailObject2);
            return new ResponseEntity<>(billOptional.get(), HttpStatus.OK);
        }
    }

    @PutMapping("/setStatusRequestMoney/{id}")
    public ResponseEntity<Bill> requestMoneyStatusBill(@PathVariable Long id) {
        Optional<Bill> billOptional = billService.findById(id);
        if (billOptional.get().getBillStatus().getId() != 3) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            Long idStatus = 6L;
            BillStatus billStatus = billStatusService.findById(idStatus).get();
            billOptional.get().setBillStatus(billStatus);
            billService.save(billOptional.get());
            MailObject mailObject2 = new MailObject("noreply@tinderwindy.com", billOptional.get().getStaff().getAccount().getEmail(), "Your Date completed", "Thank you for your co-oparation! your payment request has been processed");
            emailService.sendSimpleMessage(mailObject2);
            return new ResponseEntity<>(billOptional.get(), HttpStatus.OK);
        }
    }

//    @GetMapping("/showCheckerByUserName/{id}")
//    public ResponseEntity<BillDTO> showByChecker(@PathVariable String userName) {
//
//    }

    @GetMapping("/showByStaff/{id}")
    public ResponseEntity<Iterable<Bill>> showByStaff(@PathVariable Long id) {
        Optional<Staff> staff = staffService.findById(id);
        if (!staff.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Iterable<Bill> bills = billService.findAllByStaff(staff.get());
        if (bills == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(bills, HttpStatus.OK);
    }

    @GetMapping("/showByChecker/{id}")
    public ResponseEntity<Iterable<Bill>> showByChecker(@PathVariable Long id) {
        Optional<Checker> checker = checkerService.findById(id);
        if (!checker.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Iterable<Bill> bills = billService.findAllByChecker(checker.get());
        if (bills == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(bills, HttpStatus.OK);
    }
    @GetMapping("/showByStaffAndStatus/{idStaff}/{idStatus}")
    public ResponseEntity<Iterable<Bill>> showByChecker(@PathVariable Long idStaff,@PathVariable Long idStatus){
        Iterable<Bill> billOptional = billService.findAllByStaffIdAndBillStatusId(idStaff,idStatus);
        return new ResponseEntity<>(billOptional, HttpStatus.OK);
    }

    @GetMapping("/showAmountBill/{id1}/{id2}")
    public ResponseEntity<Bill> showAmountBill(@PathVariable Long id1, @PathVariable Long id2) {
        double amount = billService.sumAmountBill(id1, id2);
        Bill bill = new Bill();
        bill.setAmount(amount);
        bill.setStaff(staffService.findById(id1).get());
        bill.setChecker(checkerService.findById(id2).get());
        return new ResponseEntity<>(bill, HttpStatus.OK);
    }

    @GetMapping("/showBillDTO")
    public ResponseEntity<Iterable<IBillDTo>> showBillDTO() {
        Iterable<IBillDTo> billDTOS = billService.customShowBillWithContent();
        System.out.println(billDTOS);
        return new ResponseEntity<>(billDTOS, HttpStatus.OK);
    }
}
