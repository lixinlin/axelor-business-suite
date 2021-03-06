/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2017 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.bankpayment.service.bankorder;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountingSituation;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.AccountingSituationService;
import com.axelor.apps.account.service.move.MoveService;
import com.axelor.apps.account.service.payment.PaymentModeService;
import com.axelor.apps.bankpayment.db.BankOrder;
import com.axelor.apps.bankpayment.db.BankOrderLine;
import com.axelor.apps.bankpayment.db.repo.BankOrderRepository;
import com.axelor.apps.bankpayment.exception.IExceptionMessage;
import com.axelor.apps.bankpayment.service.config.AccountConfigBankPaymentService;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.administration.GeneralServiceImpl;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;

import java.lang.invoke.MethodHandles;

public class BankOrderMoveServiceImpl implements BankOrderMoveService  {
	
	private final Logger log = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );
	
	protected BankOrderRepository bankOrderRepo;
	protected MoveService moveService;
	protected PaymentModeService paymentModeService;
	protected AccountingSituationService accountingSituationService;
	protected AccountConfigBankPaymentService accountConfigBankPaymentService;
	
	protected PaymentMode paymentMode;
	protected Company senderCompany;
	protected int orderTypeSelect;
	protected int partnerTypeSelect;
	protected Journal journal;
	protected LocalDate bankOrderDate;
	protected Currency bankOrderCurrency;
	protected Account senderBankAccount;
	protected BankDetails senderBankDetails;
	protected boolean isMultiDate;
	protected boolean isMultiCurrency;
	protected boolean isDebit;
	
	@Inject
	public BankOrderMoveServiceImpl(BankOrderRepository bankOrderRepo, MoveService moveService, 
			PaymentModeService paymentModeService, 
			AccountingSituationService accountingSituationService, AccountConfigBankPaymentService accountConfigBankPaymentService)  {
		
		this.bankOrderRepo = bankOrderRepo;
		this.moveService = moveService;
		this.paymentModeService = paymentModeService;
		this.accountingSituationService = accountingSituationService;
		this.accountConfigBankPaymentService = accountConfigBankPaymentService;
		
	}
	
	
	
	public void generateMoves(BankOrder bankOrder) throws AxelorException  {
		
		if(bankOrder.getBankOrderLineList() == null || bankOrder.getBankOrderLineList().isEmpty())  {  return;  }
		
		paymentMode = bankOrder.getPaymentMode();
		
		if(paymentMode == null || !paymentMode.getGenerateMoveAutoFromBankOrder())  {  return;  }
		
		orderTypeSelect = bankOrder.getOrderTypeSelect();
		senderCompany = bankOrder.getSenderCompany();
		senderBankDetails = bankOrder.getSenderBankDetails();
		
		journal = paymentModeService.getPaymentModeJournal(paymentMode, senderCompany, senderBankDetails);
		senderBankAccount = paymentModeService.getPaymentModeAccount(paymentMode, senderCompany, senderBankDetails);
		
		isMultiDate = bankOrder.getIsMultiDate();
		isMultiCurrency = bankOrder.getIsMultiCurrency();
		
		if(orderTypeSelect == BankOrderRepository.ORDER_TYPE_INTERNATIONAL_CREDIT_TRANSFER 
				|| orderTypeSelect == BankOrderRepository.ORDER_TYPE_SEPA_CREDIT_TRANSFER)  {
			isDebit = true;
		}
		else  {
			isDebit = false;
		}
		 
		
		for(BankOrderLine bankOrderLine : bankOrder.getBankOrderLineList())  {
			
			generateMoves(bankOrderLine);
			
		}
		
	}
	
	protected void generateMoves(BankOrderLine bankOrderLine) throws AxelorException  {
		
		bankOrderLine.setSenderMove(
				generateSenderMove(bankOrderLine));
		
		if(partnerTypeSelect == BankOrderRepository.PARTNER_TYPE_COMPANY)  {
			bankOrderLine.setReceiverMove(
					generateReceiverMove(bankOrderLine));
		}
		
	}
	
	
	protected Move generateSenderMove(BankOrderLine bankOrderLine) throws AxelorException  {
		
		Partner partner = bankOrderLine.getPartner();
		
		Move senderMove = moveService.getMoveCreateService()
				.createMove(journal, senderCompany, 
						this.getCurrency(bankOrderLine), partner, 
						this.getDate(bankOrderLine), paymentMode, MoveRepository.TECHNICAL_ORIGIN_AUTOMATIC);
		
		MoveLine bankMoveLine = moveService.getMoveLineService().createMoveLine(
				senderMove, partner, senderBankAccount,
				bankOrderLine.getBankOrderAmount(), !isDebit,
				senderMove.getDate(), 1, bankOrderLine.getReceiverReference());
		senderMove.addMoveLineListItem(bankMoveLine);
		
		MoveLine partnerMoveLine = moveService.getMoveLineService().createMoveLine(
				senderMove, partner, getPartnerAccount(partner, bankOrderLine.getReceiverCompany(), senderMove.getCompany()),
				bankOrderLine.getBankOrderAmount(), isDebit,
				senderMove.getDate(), 2, bankOrderLine.getReceiverReference());
		senderMove.addMoveLineListItem(partnerMoveLine);

		return senderMove;
		
	}
	
	protected Move generateReceiverMove(BankOrderLine bankOrderLine) throws AxelorException  {
		
		Partner partner = bankOrderLine.getPartner();
		Company receiverCompany = bankOrderLine.getReceiverCompany();
		
		BankDetails receiverBankDetails = bankOrderLine.getReceiverBankDetails();
		
		Journal receiverJournal = paymentModeService.getPaymentModeJournal(paymentMode, receiverCompany, receiverBankDetails);
		Account receiverBankAccount = paymentModeService.getPaymentModeAccount(paymentMode, receiverCompany, receiverBankDetails);
		
		Move receiverMove = moveService.getMoveCreateService()
				.createMove(receiverJournal, receiverCompany, 
						this.getCurrency(bankOrderLine), partner, 
						this.getDate(bankOrderLine), paymentMode, MoveRepository.TECHNICAL_ORIGIN_AUTOMATIC);
		
		MoveLine bankMoveLine = moveService.getMoveLineService().createMoveLine(
				receiverMove, partner, receiverBankAccount,
				bankOrderLine.getBankOrderAmount(), isDebit,
				receiverMove.getDate(), 1, bankOrderLine.getReceiverReference());
		receiverMove.addMoveLineListItem(bankMoveLine);
		
		MoveLine partnerMoveLine = moveService.getMoveLineService().createMoveLine(
				receiverMove, partner, getPartnerAccount(partner, receiverCompany, receiverMove.getCompany()),
				bankOrderLine.getBankOrderAmount(), !isDebit,
				receiverMove.getDate(), 2, bankOrderLine.getReceiverReference());
		receiverMove.addMoveLineListItem(partnerMoveLine);

		return receiverMove;
		
	}
	
	
	protected Account getPartnerAccount(Partner partner, Company receiverCompany, Company moveCompany) throws AxelorException  {
		
		AccountingSituation accountingSituation = accountingSituationService.getAccountingSituation(partner, receiverCompany);

		switch (partnerTypeSelect) {
		case BankOrderRepository.PARTNER_TYPE_CUSTOMER :
			return accountingSituation.getCustomerAccount();
			
		case BankOrderRepository.PARTNER_TYPE_EMPLOYEE :
			return accountingSituation.getEmployeeAccount();
			
		case BankOrderRepository.PARTNER_TYPE_SUPPLIER :
			return accountingSituation.getSupplierAccount();
			
		case BankOrderRepository.PARTNER_TYPE_COMPANY :
			if(receiverCompany.equals(senderCompany))  {
				return accountConfigBankPaymentService.getInternalBankToBankAccount(accountConfigBankPaymentService.getAccountConfig(moveCompany));
			}
			else  {
				return accountConfigBankPaymentService.getExternalBankToBankAccount(accountConfigBankPaymentService.getAccountConfig(moveCompany));
			}

		default:
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.BANK_ORDER_PARTNER_TYPE_MISSING),
					GeneralServiceImpl.EXCEPTION), IException.CONFIGURATION_ERROR);
		}
		
	}
	
	protected LocalDate getDate(BankOrderLine bankOrderLine)  {
		
		if(isMultiDate)  {
			return bankOrderDate;
		}
		else  {
			return bankOrderLine.getBankOrderDate();
		}
		
	}
	
	protected Currency getCurrency(BankOrderLine bankOrderLine)   {
		
		if(isMultiCurrency)  {
			return bankOrderCurrency;
		}
		else  {
			return bankOrderLine.getBankOrderCurrency();
		}
		
	}
	
	
}





