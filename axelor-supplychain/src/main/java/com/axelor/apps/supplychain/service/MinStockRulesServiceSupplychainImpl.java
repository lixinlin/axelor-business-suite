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
package com.axelor.apps.supplychain.service;

import java.io.IOException;
import java.math.BigDecimal;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.message.db.Message;
import com.axelor.apps.message.db.Template;
import com.axelor.apps.message.db.repo.MessageRepository;
import com.axelor.apps.message.db.repo.TemplateRepository;
import com.axelor.apps.message.service.TemplateMessageService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.purchase.service.PurchaseOrderLineService;
import com.axelor.apps.purchase.service.config.PurchaseConfigService;
import com.axelor.apps.stock.db.Location;
import com.axelor.apps.stock.db.LocationLine;
import com.axelor.apps.stock.db.MinStockRules;
import com.axelor.apps.stock.db.repo.MinStockRulesRepository;
import com.axelor.apps.stock.service.MinStockRulesServiceImpl;
import com.axelor.auth.db.User;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class MinStockRulesServiceSupplychainImpl extends MinStockRulesServiceImpl  {

	@Inject
	protected PurchaseOrderServiceSupplychainImpl purchaseOrderServiceSupplychainImpl;

	@Inject
	protected PurchaseOrderLineService purchaseOrderLineService;

	@Inject
	protected PurchaseConfigService purchaseConfigService;

	protected User user;

	@Inject
	private PurchaseOrderRepository purchaseOrderRepo;
	
	@Inject
	private TemplateRepository templateRepo;
	
	@Inject
	private TemplateMessageService templateMessageService;
	
	@Inject
	private MessageRepository messageRepo;

	@Override
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void generatePurchaseOrder(Product product, BigDecimal qty, LocationLine locationLine, int type) throws AxelorException  {

		Location location = locationLine.getLocation();

		//TODO à supprimer après suppression des variantes
		if(location == null)  {
			return;
		}

		MinStockRules minStockRules = this.getMinStockRules(product, location, type);

		if(minStockRules == null)  {
			return;
		}

		if(this.useMinStockRules(locationLine, minStockRules, qty, type))  {

			if(minStockRules.getOrderAlertSelect() ==  MinStockRulesRepository.ORDER_ALERT_ALERT)  {
				
				Template template = templateRepo.all().filter("self.metaModel.fullName = ?1 AND self.isSystem != true",  MinStockRules.class.getName()).fetchOne();
				try {
					Message message = templateMessageService.generateMessage(minStockRules, template);
					messageRepo.save(message);
//					if (JPA.em().getTransaction().isActive()) {
//						JPA.em().getTransaction().commit();
//						JPA.em().getTransaction().begin();
//					}
				} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IOException e) {
					throw new AxelorException(e, IException.TECHNICAL);
				}
			}
			else if(minStockRules.getOrderAlertSelect() == MinStockRulesRepository.ORDER_ALERT_PRODUCTION_ORDER)  {


			}
			else if(minStockRules.getOrderAlertSelect() == MinStockRulesRepository.ORDER_ALERT_PURCHASE_ORDER)  {

				Partner supplierPartner = product.getDefaultSupplierPartner();

				if(supplierPartner != null)  {

					Company company = location.getCompany();

					PurchaseOrder purchaseOrder = purchaseOrderRepo.save(purchaseOrderServiceSupplychainImpl.createPurchaseOrder(
							this.user,
							company,
							null,
							supplierPartner.getCurrency(),
							this.today.plusDays(supplierPartner.getDeliveryDelay()),
							minStockRules.getName(),
							null,
							location,
							this.today,
							supplierPartner.getPurchasePriceList(),
							supplierPartner));

					purchaseOrder.addPurchaseOrderLineListItem(
							purchaseOrderLineService.createPurchaseOrderLine(
									purchaseOrder,
									product,
									null,
									null,
									minStockRules.getReOrderQty(),
									product.getUnit()));

					purchaseOrderServiceSupplychainImpl.computePurchaseOrder(purchaseOrder);

					purchaseOrderRepo.save(purchaseOrder);

				}


			}




		}

	}


}
