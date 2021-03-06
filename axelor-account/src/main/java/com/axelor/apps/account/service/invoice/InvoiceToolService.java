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
package com.axelor.apps.account.service.invoice;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.PaymentCondition;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.db.repo.PaymentConditionRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.db.Partner;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import org.joda.time.LocalDate;

import java.math.BigDecimal;

/**
 * InvoiceService est une classe implémentant l'ensemble des services de
 * facturations.
 *
 */
public class InvoiceToolService {


	public static LocalDate getDueDate(PaymentCondition paymentCondition, LocalDate invoiceDate)  {
		
		switch (paymentCondition.getTypeSelect()) {
		case PaymentConditionRepository.TYPE_NET:
			
			return invoiceDate.plusDays(paymentCondition.getPaymentTime());
			
		case PaymentConditionRepository.TYPE_END_OF_MONTH_N_DAYS:
					
			return invoiceDate.dayOfMonth().withMaximumValue().plusDays(paymentCondition.getPaymentTime());
					
		case PaymentConditionRepository.TYPE_N_DAYS_END_OF_MONTH:
			
			return invoiceDate.plusDays(paymentCondition.getPaymentTime()).dayOfMonth().withMaximumValue();
			
		case PaymentConditionRepository.TYPE_N_DAYS_END_OF_MONTH_AT:
			
			return invoiceDate.plusDays(paymentCondition.getPaymentTime()).dayOfMonth().withMaximumValue().plusDays(paymentCondition.getDaySelect());

		default:
			return invoiceDate;
		}
		
	}
	
	/**
	 * 
	 * @param invoice
	 * 
	 * OperationTypeSelect
	 *  1 : Achat fournisseur
	 *	2 : Avoir fournisseur
	 *	3 : Vente client
	 *	4 : Avoir client
	 * @return
	 * @throws AxelorException
	 */
	public static boolean isPurchase(Invoice invoice) throws AxelorException  {
		
		boolean isPurchase;
		
		switch(invoice.getOperationTypeSelect())  {
		case InvoiceRepository.OPERATION_TYPE_SUPPLIER_PURCHASE :
			isPurchase = true;
			break;
		case InvoiceRepository.OPERATION_TYPE_SUPPLIER_REFUND :
			isPurchase = true;
			break;
		case InvoiceRepository.OPERATION_TYPE_CLIENT_SALE :
			isPurchase = false;
			break;
		case InvoiceRepository.OPERATION_TYPE_CLIENT_REFUND :
			isPurchase = false;
			break;
		
		default:
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.MOVE_1), invoice.getInvoiceId()), IException.MISSING_FIELD);
		}	
		
		return isPurchase;
	}
	
	
	/**
	 * 
	 * @param invoice
	 * 
	 * OperationTypeSelect
	 *  1 : Achat fournisseur
	 *	2 : Avoir fournisseur
	 *	3 : Vente client
	 *	4 : Avoir client
	 * @return
	 * @throws AxelorException
	 */
	public static boolean isRefund(Invoice invoice) throws AxelorException  {
		
		boolean isRefund;
		
		switch(invoice.getOperationTypeSelect())  {
		case InvoiceRepository.OPERATION_TYPE_SUPPLIER_PURCHASE :
			isRefund = false;
			break;
		case InvoiceRepository.OPERATION_TYPE_SUPPLIER_REFUND :
			isRefund = true;
			break;
		case InvoiceRepository.OPERATION_TYPE_CLIENT_SALE :
			isRefund = false;
			break;
		case InvoiceRepository.OPERATION_TYPE_CLIENT_REFUND :
			isRefund = true;
			break;
		
		default:
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.MOVE_1), invoice.getInvoiceId()), IException.MISSING_FIELD);
		}	
		
		return isRefund;
	}
	
	
	/**
	 * @param invoice
	 * @return
	 * @throws AxelorException
	 */
	public static boolean isOutPayment(Invoice invoice) throws AxelorException {
		if (invoice.getInTaxTotal().compareTo(BigDecimal.ZERO) >= 0) {
			// result of XOR operator, we could also have written "bool1 ^ bool2"
			return (isPurchase(invoice) != isRefund(invoice));
		} else {
			// return opposite if total amount is negative
			return (isPurchase(invoice) == isRefund(invoice));
		}
	}


	public static PaymentMode getPaymentMode(Invoice invoice) throws AxelorException {
		Partner partner = invoice.getPartner();

		if (InvoiceToolService.isOutPayment(invoice)) {
			if (partner != null) {
				PaymentMode paymentMode = partner.getOutPaymentMode();
				if (paymentMode != null) {
					return paymentMode;
				}
			}
			return Beans.get(AccountConfigService.class).getAccountConfig(invoice.getCompany()).getOutPaymentMode();
		} else {
			if (partner != null) {
				PaymentMode paymentMode = partner.getInPaymentMode();
				if (paymentMode != null) {
					return paymentMode;
				}
			}
			return Beans.get(AccountConfigService.class).getAccountConfig(invoice.getCompany()).getInPaymentMode();
		}
	}

	public static PaymentCondition getPaymentCondition(Invoice invoice) throws AxelorException {
		Partner partner = invoice.getPartner();
		
		if (partner != null) {
			PaymentCondition paymentCondition = partner.getPaymentCondition();
			if (paymentCondition != null) {
				return paymentCondition;
			}
		}
		return Beans.get(AccountConfigService.class).
				getAccountConfig(invoice.getCompany()).getDefPaymentCondition();
		
	}
	
	
}
