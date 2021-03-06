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
package com.axelor.apps.account.service;

import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.account.db.repo.ReconcileRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.base.db.IAdministration;
import com.axelor.apps.base.service.administration.GeneralServiceImpl;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.auth.AuthUtils;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.common.base.Strings;
import com.google.inject.Inject;

public class ReconcileSequenceService {
	
	protected SequenceService sequenceService;

	@Inject
	public ReconcileSequenceService(SequenceService sequenceService) {

		this.sequenceService = sequenceService;
		
	}
	
	public void setSequence(Reconcile reconcile)  throws AxelorException  {
		reconcile.setReconcileSeq(this.getSequence(reconcile));
	}

	protected String getSequence(Reconcile reconcile) throws AxelorException  {

		SequenceService sequenceService = Beans.get(SequenceService.class);
		String seq = sequenceService.getSequenceNumber(IAdministration.RECONCILE, reconcile.getDebitMoveLine().getMove().getCompany());
		if(seq == null)  {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.RECONCILE_6),
					GeneralServiceImpl.EXCEPTION, AuthUtils.getUser().getActiveCompany().getName()), IException.CONFIGURATION_ERROR);
		}
		return seq;
	}

	public void setDraftSequence(Reconcile reconcile)  {		
			
		if (reconcile.getId() != null && Strings.isNullOrEmpty(reconcile.getReconcileSeq())
			&& reconcile.getStatusSelect() == ReconcileRepository.STATUS_DRAFT)  {		
			reconcile.setReconcileSeq(this.getDraftSequence(reconcile));		
		}		
		
	}	
	
	protected String getDraftSequence(Reconcile reconcile)  {		
		return "*" + reconcile.getId();		
	}		
	
}
