//USING ANDROID-SQLITE DATABASES
package cis470.matos.databases;

import android.app.Activity;
import android.app.ProgressDialog;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import org.xmlpull.v1.XmlPullParser;

public class SQLDemo2 extends Activity
{
	Button btnGoParser;
	Button buttondb;

	SQLiteDatabase db;
	TextView txtMsg;
	int i = 0;
	String[] tag = new String[500];

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		txtMsg = (TextView) findViewById(R.id.txtMsg);
		buttondb = (Button) findViewById(R.id.buttondb);
		btnGoParser = (Button) findViewById(R.id.btnReadXml);
		btnGoParser.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					openDatabase(); // open (create if needed) database
					dropTable(); // if needed drop table tblAmigos
					readData();
					insertSomeDbData();
					useRawQueryShowAll(); // display all records
					//db.close(); // make sure to release the DB
					txtMsg.append("\nAll Done!");

					Toast.makeText(getBaseContext(),
							"Done writing to DATABASE", Toast.LENGTH_SHORT).show();
				} catch (Exception e) {
					txtMsg.append("\nError onCreate: " + e.getMessage());
					finish();
				}
			}
		}); // onCreate
		buttondb.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				try {
					// hard-coded SQL select with no arguments
					String mySQL = "select * from tblAMIGO";
					Cursor c1 = db.rawQuery(mySQL, null);

					// get the first recID
					c1.moveToFirst();
					int index = c1.getColumnIndex("recID");
					int theRecID = c1.getInt(index);

					txtMsg.append("\n-useRawQuery1 - first recID  " + theRecID);
					txtMsg.append("\n-useRawQuery1" + showCursor(c1) );

				} catch (Exception e) {
					txtMsg.append("\nError useRawQuery1: " + e.getMessage());

				}

				/*
				Cursor cursor = db.query(, new String[] {"_id", "title", "title_raw"},
						"title_raw like " + "'%Smith%'", null, null, null, null);
						*/

			}// onClick
		});
	}
	// /////////////////////////////////////////////////////////////////
	private void openDatabase()
	{
		try
		{
			// path to private memory:
			String SDcardPath = "data/data/cis470.matos.databases";
			// -----------------------------------------------------------
			// this provides the path name to the SD card
			// String SDcardPath = Environment.getExternalStorageDirectory().getPath();

			String myDbPath = SDcardPath + "/" + "myfriendsDB2.db";
			txtMsg.append("\n-openDatabase - DB Path: " + myDbPath);

			db = SQLiteDatabase.openDatabase(myDbPath, null,
					SQLiteDatabase.CREATE_IF_NECESSARY);

			txtMsg.append("\n-openDatabase - DB was opened");
		}

		catch (SQLiteException e)
		{
			txtMsg.append("\nError openDatabase: " + e.getMessage());
			finish();
		}
	}// createDatabase

	// ///////////////////////////////////////////////////////////////////
	private void dropTable()
	{
		// (clean start) action query to drop table
		try
		{
			db.execSQL("DROP TABLE IF EXISTS tblAmigo;");
			txtMsg.append("\n-dropTable - dropped!!");
		}

		catch (Exception e)
		{
			txtMsg.append("\nError dropTable: " + e.getMessage());
			finish();
		}
	}

	// ///////////////////////////////////////////////////////////////////
	private void readData()
	{
		Integer xmlResFile = R.xml.employee1;
		new backgroundAsyncTask().execute(xmlResFile);
	}

	public class backgroundAsyncTask extends AsyncTask<Integer, Void, StringBuilder>
	{
		ProgressDialog dialog = new ProgressDialog(SQLDemo2.this);

		@Override
		protected void onPostExecute(StringBuilder result)
		{
			super.onPostExecute(result);
			dialog.dismiss();
			txtMsg.setText(result.toString());
		}

		@Override
		protected void onPreExecute()
		{
			super.onPreExecute();
			dialog.setMessage("Please wait...");
			dialog.setCancelable(false);
			dialog.show();
		}

		@Override
		protected void onProgressUpdate(Void... values)
		{
			super.onProgressUpdate(values);
		}

		@Override
		protected StringBuilder doInBackground(Integer... params)
		{
			int xmlResFile = params[0];

			XmlPullParser parser = getResources().getXml(xmlResFile);

			StringBuilder stringBuilder = new StringBuilder();
			String nodeText = "";
			String nodeName = "";

			try
			{
				int eventType = -1;

				while (eventType != XmlPullParser.END_DOCUMENT)
				{
					eventType = parser.next();

					if (eventType == XmlPullParser.START_DOCUMENT)
					{
						stringBuilder.append("\nSTART_DOCUMENT");
					}

					else if (eventType == XmlPullParser.END_DOCUMENT)
					{
						stringBuilder.append("\nEND_DOCUMENT");
					}

					else if (eventType == XmlPullParser.START_TAG)
					{
						nodeName = parser.getName();
						stringBuilder.append("\nSTART_TAG: " + nodeName);

						stringBuilder.append(getAttributes(parser));
					}

					else if (eventType == XmlPullParser.END_TAG)
					{
						nodeName = parser.getName();
						stringBuilder.append("\nEND_TAG:   " + nodeName);
					}

					else if (eventType == XmlPullParser.TEXT)
					{
						nodeText = parser.getText();
						stringBuilder.append("\n    TEXT: " + nodeText);
						//tagname[i] =
								tag[i] = nodeText;
						i++;

					}
				}
			}

			catch (Exception e)
			{
				Log.e("<<PARSING ERROR>>", e.getMessage());
			}

			return stringBuilder;
		}// doInBackground

		private String getAttributes(XmlPullParser parser)
		{
			StringBuilder stringBuilder = new StringBuilder();
			// trying to detect inner attributes nested inside a node tag
			String name = parser.getName();

			if (name != null)
			{
				int size = parser.getAttributeCount();

				for (int i = 0; i < size; i++)
				{
					String attrName = parser.getAttributeName(i);
					String attrValue = parser.getAttributeValue(i);

					stringBuilder.append("\n    Attrib <key,value>= "
							+ attrName + ", " + attrValue);
				}
			}

			return stringBuilder.toString();

		}// innerElements
	}

	// /////////////////////////////////////////////////////////////////
	private void insertSomeDbData()
	{
		// create table: tblAmigo
		db.beginTransaction();

		try
		{
			// create table
			db.execSQL("create table tblAMIGO ("

					+ " recID integer PRIMARY KEY autoincrement, "
					+ " name  text, "
					+ " mname  text, "
					+ "  age text, "
					+ "salary text );  ");

					/*
					+ " name text, "
					+ " Middle text, "
					+ " Last text, "
					+ " SSN text,"
					+ " DOB text,"
					+ " address text,"
					+ " sex text,"
					+ " salary text,"
					+ " superssn text,"
					+ " dno text); ");
					*/
			// commit your changes
			db.setTransactionSuccessful();

			txtMsg.append("\n-insertSomeDbData - Table was created");
		}

		catch (SQLException e1)
		{
			txtMsg.append("\nError insertSomeDbData: " + e1.getMessage());
			finish();
		}

		finally
		{
			db.endTransaction();
		}

		// populate table: tblAmigo
		db.beginTransaction();
		try
		{
			// insert rows
/*
			db.execSQL("insert into tblAMIGO(name, Middle, Last, SSN, DOB, address, sex, salary, superssn, dno) "
					+ " values ('"+tag[0]+"', '"+tag[1]+"', '"+tag[2]+"', '"+tag[3]+"', '"+tag[4]+"', '"+tag[5]+"' ), '"+tag[6]+"', '"+tag[7]+"', '"+tag[8]+"', '"+tag[9]+"';");

			*/
			db.execSQL("insert into tblAMIGO(name, mname, age, salary) "
					+ " values ('"+ tag[0]+"', '"+ tag[1]+"', '"+ tag[2]+"','"+ tag[3]+"' );");

			db.execSQL("insert into tblAMIGO(name, mname, age, salary) "
					+ " values ('"+ tag[4]+"', '"+ tag[5]+"', '"+ tag[6]+"','"+ tag[7]+"' );");
			db.execSQL("insert into tblAMIGO(name, mname, age, salary) "
					+ " values ('"+ tag[8]+"', '"+ tag[9]+"', '"+ tag[10]+"','"+ tag[11]+"' );");
			db.execSQL("insert into tblAMIGO(name, mname, age, salary) "
					+ " values ('"+ tag[12]+"', '"+ tag[13]+"', '"+ tag[14]+"','"+ tag[15]+"' );");


			// commit your changes
			db.setTransactionSuccessful();
			txtMsg.append("\n-insertSomeDbData - 3 rec. were inserted");
		}

		catch (SQLiteException e2)
		{
			txtMsg.append("\nError insertSomeDbData: " + e2.getMessage());
		}

		finally
		{
			db.endTransaction();
		}

	}// insertSomeData


	// ///////////////////////////////////////////////////////////////
	private void useRawQueryShowAll()
	{
		try
		{
			// hard-coded SQL select with no arguments
			String mySQL = "select * from tblAMIGO";
			Cursor c1 = db.rawQuery(mySQL, null);

			txtMsg.append("\n-useRawQueryShowAll" + showCursor(c1));
		}
		catch (Exception e)
		{
			txtMsg.append("\nError useRawQuery1: " + e.getMessage());
		}
	}// useRawQuery1


	// ///////////////////////////////////////////////////////////

	private String showCursor(Cursor cursor)
	{
		// show SCHEMA (column names & types)
		cursor.moveToPosition(-1); //reset cursor's top
		String cursorData = "\nCursor: [";

		try
		{
			// get column names
			String[] colName = cursor.getColumnNames();
			for (int i = 0; i < colName.length; i++)
			{
				String dataType = getColumnType(cursor, i);
				cursorData += colName[i] + dataType;

				if (i < colName.length - 1)
				{
					cursorData += ", ";
				}
			}
		}

		catch (Exception e)
		{
			Log.e("<<SCHEMA>>", e.getMessage());
		}
		cursorData += "]";

		// now get the rows
		cursor.moveToPosition(-1); //reset cursor's top

		while (cursor.moveToNext())
		{
			String cursorRow = "\n[";

			for (int i = 0; i < cursor.getColumnCount(); i++)
			{
				cursorRow += cursor.getString(i);

				if (i < cursor.getColumnCount() - 1)
					cursorRow += ", ";
			}

			cursorData += cursorRow + "]";
		}

		return cursorData + "\n";
	}

	private String getColumnType(Cursor cursor, int i)
	{
		try
		{
			//peek at a row holding valid data
			cursor.moveToFirst();
			int result = cursor.getType(i);
			String[] types = {":NULL", ":INT", ":FLOAT", ":STR", ":BLOB", ":UNK"};
			//backtrack - reset cursor's top
			cursor.moveToPosition(-1);
			return types[result];
		}

		catch (Exception e)
		{
			return " ";
		}
	}

	private void useRawQuery1() {
		try {
			// hard-coded SQL select with no arguments
			String mySQL = "select * from tblAMIGO";
			Cursor c1 = db.rawQuery(mySQL, null);

			// get the first recID
			c1.moveToFirst();
			int index = c1.getColumnIndex("recID");
			int theRecID = c1.getInt(index);

			txtMsg.append("\n-useRawQuery1 - first recID  " + theRecID);
			txtMsg.append("\n-useRawQuery1" + showCursor(c1) );

		} catch (Exception e) {
			txtMsg.append("\nError useRawQuery1: " + e.getMessage());

		}
	}// useRa



}// class