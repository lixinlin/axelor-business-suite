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
/**
 *
 */
package com.axelor.apps.stock.exception;

/**
 * @author axelor
 *
 */
public interface IExceptionMessage {

	/**
	 * Inventory service and controller
	 */
	static final String INVENTORY_1 = /*$$(*/ "You must select a stock location" /*)*/;
	static final String INVENTORY_2 = /*$$(*/ "There's no configured sequence for inventory for company" /*)*/;
	static final String INVENTORY_3 = /*$$(*/ "An error occurred while importing the file data. Please contact your application administrator to check Traceback logs." /*)*/;
	static final String INVENTORY_4 = /*$$(*/ "An error occurred while importing the file data, product not found with code :" /*)*/;
	static final String INVENTORY_5 = /*$$(*/ "There is currently no such file in the specified folder or the folder may not exists." /*)*/;
	static final String INVENTORY_6 = /*$$(*/ "Company missing for stock location %s" /*)*/;
	static final String INVENTORY_7 = /*$$(*/ "Incorrect product in inventory line" /*)*/;
	static final String INVENTORY_8 = /*$$(*/ "File %s successfully imported." /*)*/;
	static final String INVENTORY_9 = /*$$(*/ "There's no product in stock location." /*)*/;
	static final String INVENTORY_10 = /*$$(*/ "Inventory's lines' list has been filled." /*)*/;
	static final String INVENTORY_11 = /*$$(*/ "No inventory lines has been created." /*)*/;
	static final String INVENTORY_12 = /*$$(*/ "An error occurred while importing the file data, there are multiple products with code :" /*)*/;

	/**
	 * Location Line Service Impl
	 */
	static final String LOCATION_LINE_1 = /*$$(*/ "Product's stocks %s (%s) are not in sufficient quantity to realize the delivery" /*)*/;
	static final String LOCATION_LINE_2 = /*$$(*/ "Product's stocks %s (%s), tracking number {} are not in sufficient quantity to realize the delivery" /*)*/;

	/**
	 * Stock Move Service and Controller
	 */
	static final String STOCK_MOVE_1 = /*$$(*/ "There's no configured sequence for stock's intern moves for the company %s" /*)*/;
	static final String STOCK_MOVE_2 = /*$$(*/ "There's no configured sequence for stock's receptions for the company %s" /*)*/;
	static final String STOCK_MOVE_3 = /*$$(*/ "There's no configured sequence for stock's delivery for the company %s" /*)*/;
	static final String STOCK_MOVE_4 = /*$$(*/ "Stock's movement's type undefined" /*)*/;
	static final String STOCK_MOVE_5 = /*$$(*/ "There's no source location selected for the stock's movement %s" /*)*/;
	static final String STOCK_MOVE_6 = /*$$(*/ "There's no destination location selected for the stock's movement %s" /*)*/;
	static final String STOCK_MOVE_7 = /*$$(*/ "Partial stock move (From" /*)*/;
	static final String STOCK_MOVE_8 = /*$$(*/ "Reverse stock move (From" /*)*/;
	static final String STOCK_MOVE_9 = /*$$(*/ "A partial stock move has been generated (%s)" /*)*/;
	static final String STOCK_MOVE_10 = /*$$(*/ "Please select the StockMove(s) to print." /*)*/;
	static final String STOCK_MOVE_11 = /*$$(*/ "Company address is empty." /*)*/;
	static final String STOCK_MOVE_12 = /*$$(*/ "Feature currently not available with Open Street Maps." /*)*/;
	static final String STOCK_MOVE_13 = /*$$(*/ "<B>%s or %s</B> not found" /*)*/;
	static final String STOCK_MOVE_14 = /*$$(*/ "No move lines to split" /*)*/;
	static final String STOCK_MOVE_15 = /*$$(*/ "Please select lines to split" /*)*/;
	static final String STOCK_MOVE_16 = /*$$(*/ "Please entry proper split qty" /*)*/;
	static final String STOCK_MOVE_SPLIT_NOT_GENERATED = /*$$(*/ "No new stock move was generated" /*)*/;
	static final String STOCK_MOVE_INCOMING_PARTIAL_GENERATED = /*$$(*/ "An incoming partial stock move has been generated (%s)" /*)*/;
	static final String STOCK_MOVE_OUTGOING_PARTIAL_GENERATED = /*$$(*/ "An outgoing partial stock move has been generated (%s)" /*)*/;

	/**
	 * Tracking Number Service
	 */
	static final String TRACKING_NUMBER_1 = /*$$(*/ "There's no configured sequence for tracking number for the product %s:%s" /*)*/;

	/**
	 * Stock Config Service
	 */
	static final String STOCK_CONFIG_1 = /*$$(*/ "You must configure a Stock module for the company %s" /*)*/;
	static final String STOCK_CONFIG_2 = /*$$(*/ "You must configure an inventory virtual location for the company %s" /*)*/;
	static final String STOCK_CONFIG_3 = /*$$(*/ "You must configure a supplier virtual location for the company %s" /*)*/;
	static final String STOCK_CONFIG_4 = /*$$(*/ "You must configure a customer virtual location for the company %s" /*)*/;

	/**
	 * Location Controller
	 */
	static final String LOCATION_1 = /*$$(*/ "There's already an existing storage, you must deactivate it first" /*)*/;
	static final String LOCATION_2 = /*$$(*/ "Please select the Stock Location(s) to print." /*)*/;


}