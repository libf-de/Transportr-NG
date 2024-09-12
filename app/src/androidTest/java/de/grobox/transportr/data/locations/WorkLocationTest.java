/*
 *    Transportr
 *
 *    Copyright (c) 2013 - 2021 Torsten Grote
 *
 *    This program is Free Software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as
 *    published by the Free Software Foundation, either version 3 of the
 *    License, or (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.grobox.transportr.data.locations;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import de.grobox.transportr.data.DbTest;
import de.libf.ptek.dto.Location;
import de.libf.ptek.dto.Point;
import de.libf.ptek.dto.Product;

import static de.libf.ptek.NetworkId.DB;
import static de.libf.ptek.dto.LocationType.ADDRESS;
import static de.libf.ptek.dto.LocationType.STATION;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(AndroidJUnit4.class)
public class WorLocationTest extends DbTest {

	private LocationDao dao;

	@Before
	@Override
	public void createDb() throws Exception {
		super.createDb();
		dao = db.locationDao();
	}

	@Test
	public void insertWorLocation() throws Exception {
		// no home location should exist
		assertNull(getValue(dao.getWorLocation(DB)));

		// create a complete station location
		Location location = new Location(STATION, "stationId", Point.from1E6(23, 42), "place", "name", Product.ALL);
		long uid1 = dao.addWorLocation(new WorLocation(DB, location));

		// assert that location has been inserted and retrieved properly
		WorLocation worLocation = getValue(dao.getWorLocation(DB));
		assertNotNull(worLocation);
		assertEquals(uid1, worLocation.uid);
		assertEquals(DB, worLocation.networkId);
		assertEquals(location.type, worLocation.type);
		assertEquals(location.id, worLocation.id);
		assertEquals(location.getLatAs1E6(), worLocation.lat);
		assertEquals(location.getLonAs1E6(), worLocation.lon);
		assertEquals(location.place, worLocation.place);
		assertEquals(location.name, worLocation.name);
		assertEquals(location.products, worLocation.products);

		// create a different home location
		location = new Location(ADDRESS, null, Point.from1E6(1337, 0), "place2", "name2", null);
		dao.addWorLocation(new WorLocation(DB, location));

		// assert that old home location has been replaced properly
		assertEquals(1, dao.countWorks(DB));
		worLocation = getValue(dao.getWorLocation(DB));
		assertNotNull(worLocation);
		assertEquals(DB, worLocation.networkId);
		assertEquals(location.type, worLocation.type);
		assertEquals(location.id, worLocation.id);
		assertEquals(location.getLatAs1E6(), worLocation.lat);
		assertEquals(location.getLonAs1E6(), worLocation.lon);
		assertEquals(location.place, worLocation.place);
		assertEquals(location.name, worLocation.name);
		assertEquals(location.products, worLocation.products);
	}

}
