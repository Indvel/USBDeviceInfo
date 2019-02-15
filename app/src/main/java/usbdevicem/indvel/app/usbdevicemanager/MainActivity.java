package usbdevicem.indvel.app.usbdevicemanager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity {

    ImageView img;
    ListView list;
    BroadcastReceiver mUsbAttachReceiver = null;
    BroadcastReceiver mUsbDetachReceiver = null;
    UsbManager mUsbManager = null;

    ArrayList<InfoData> infos = new ArrayList<InfoData>();
    InfoAdapter adapter;

    LogoTask task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        img = (ImageView) findViewById(R.id.logoImg);
        list = (ListView) findViewById(R.id.listView);

        infos.add(0, new InfoData("Searching USB device...", ""));

        adapter = new InfoAdapter(this);
        list.setAdapter(adapter);

        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

        BroadcastReceiver mUsbAttachReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

                if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                    infos.clear();
                    if(mUsbManager != null) {
                        HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
                        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
                        while (deviceIterator.hasNext()) {
                            UsbDevice device = deviceIterator.next();
                            infos.add(0, new InfoData("Device Name", device.getDeviceName()));
                            infos.add(1, new InfoData("Device ID", String.valueOf(device.getDeviceId())));
                            infos.add(2, new InfoData("Device Protocol", String.valueOf(device.getDeviceProtocol())));
                            if (Build.VERSION.SDK_INT >= 21) {
                                infos.add(3, new InfoData("Manufacturer Name", device.getManufacturerName()));
                                infos.add(4, new InfoData("Product Name", device.getProductName()));
                                infos.add(5, new InfoData("Serial Number", device.getSerialNumber()));
                            } else {
                                infos.add(3, new InfoData("Manufacturer Name", "None"));
                                infos.add(4, new InfoData("Product Name", "None"));
                                infos.add(5, new InfoData("Serial Number", "None"));
                            }
                            if (Build.VERSION.SDK_INT >= 23) {
                                infos.add(6, new InfoData("Version", "USB " + device.getVersion()));
                            } else {
                                infos.add(6, new InfoData("Version", "None"));
                            }
                            infos.add(7, new InfoData("Vendor ID", String.valueOf(device.getVendorId())));
                        }
                        if(infos.get(3).value.length() != 0) {
                            if(infos.get(3).value.toLowerCase().contains("logitech")) {
                                task = new LogoTask();
                                task.execute("http://logo.clearbit.com/logitechg.com");
                            } else if(infos.get(3).value.toLowerCase().contains("toshiba")) {
                                task = new LogoTask();
                                task.execute("http://logo.clearbit.com/exploravision.org");
                            } else {
                                task = new LogoTask();
                                task.execute("http://logo.clearbit.com/" + infos.get(3).value.toLowerCase().replace(" ", "") + ".com");
                            }
                        }
                    }
                    adapter.notifyDataSetChanged();
                    list.setAdapter(adapter);
                }
            }
        };

        BroadcastReceiver mUsbDetachReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

                if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                        infos.clear();
                        infos.add(0, new InfoData("Searching USB device...", ""));
                        img.setImageBitmap(null);
                        img.setVisibility(View.GONE);
                        adapter.notifyDataSetChanged();
                        list.setAdapter(adapter);
                }
            }
        };
        registerReceiver(mUsbAttachReceiver, new IntentFilter(UsbManager.ACTION_USB_DEVICE_ATTACHED));
        registerReceiver(mUsbDetachReceiver, new IntentFilter(UsbManager.ACTION_USB_DEVICE_DETACHED));
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mUsbAttachReceiver, new IntentFilter(UsbManager.ACTION_USB_DEVICE_ATTACHED));
        registerReceiver(mUsbDetachReceiver, new IntentFilter(UsbManager.ACTION_USB_DEVICE_DETACHED));
    }

    @Override
    protected void onPause() {
        super.onPause();
        registerReceiver(mUsbAttachReceiver, new IntentFilter(UsbManager.ACTION_USB_DEVICE_ATTACHED));
        registerReceiver(mUsbDetachReceiver, new IntentFilter(UsbManager.ACTION_USB_DEVICE_DETACHED));
    }

    public class LogoTask extends AsyncTask<String, Void, String> {

        Bitmap bit;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        @Override
        protected String doInBackground(String... strings) {
            try {

                HttpURLConnection ec = (HttpURLConnection) new URL(strings[0]).openConnection();
                ec.setRequestMethod("GET");
                ec.setDoInput(true);
                ec.setDoOutput(false);
                ec.connect();

                if(ec.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    bit = BitmapFactory.decodeStream(ec.getInputStream());
                    bit = Bitmap.createScaledBitmap(bit, (int)Math.pow(bit.getWidth(), 1.2), (int)Math.pow(bit.getHeight(), 1.2), true);
                } else {
                    bit = null;
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if(bit != null) {
                img.setVisibility(View.VISIBLE);
                img.setImageBitmap(bit);
            }
        }
    }

    class ViewHolder {

        public TextView mName;
        public TextView mValue;
    }

    public class InfoAdapter extends BaseAdapter {
        private Context mContext = null;

        public InfoAdapter(Context mContext) {
            this.mContext = mContext;
        }

        @Override
        public int getCount() {
            return infos.size();
        }

        @Override
        public Object getItem(int position) {
            return infos.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;

            if (convertView == null) {
                holder = new ViewHolder();

                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.info_item, null);

                holder.mName = (TextView) convertView.findViewById(R.id.textName);
                holder.mValue = (TextView) convertView.findViewById(R.id.textValue);
                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            InfoData mData = infos.get(position);

            holder.mName.setText(mData.name);
            holder.mValue.setText(mData.value);

            return convertView;
        }
    }

    public class InfoData { // 데이터를 받는 클래스

        public String name;
        public String value;

        public InfoData(String name, String value) { //데이터를 받는 클래스 메서드
            this.name = name;
            this.value = value;
        }
    }
}
