package com.apprevelations.frontburner;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Created by appyware on 22/03/15.
 */
public class HomeFragment extends Fragment {

    TextView textview;
    NodeList nodelist;
    ProgressDialog pDialog;
    ListView listView;
    List<XmlItem> items;
    private View view;

    String URL = "http://164.100.47.5/AndroidFeeds/QuestionList.aspx?member_id=2045";

    public static final String CLASS_NAME = "HomeFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_home, container, false);
            listView = (ListView) view.findViewById(R.id.listView);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    XmlAdapter adapter = (XmlAdapter) parent.getAdapter();
                    XmlItem item = (XmlItem) adapter.getItem(position);
                    Uri uri = Uri.parse(item.getLink());
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                }
            });
        } else {
            // If we are returning from a configuration change:
            // "view" is still attached to the previous view hierarchy
            // so we need to remove it and re-attach it to the current one
            ViewGroup parent = (ViewGroup) view.getParent();
            parent.removeView(view);
        }
        new DownloadXML().execute(URL);
        return view;
    }

    // DownloadXML AsyncTask
    private class DownloadXML extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Create a progressbar
            pDialog = new ProgressDialog(getActivity());
            // Set progressbar title
            pDialog.setTitle("Android Simple XML Parsing using DOM Tutorial");
            // Set progressbar message
            pDialog.setMessage("Loading...");
            pDialog.setIndeterminate(false);
            // Show progressbar
            pDialog.show();
        }

        @Override
        protected Void doInBackground(String... Url) {
            try {
                java.net.URL url = new URL(Url[0]);
                DocumentBuilderFactory dbf = DocumentBuilderFactory
                        .newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                // Download the XML file
                Document doc = db.parse(new InputSource(url.openStream()));
                doc.getDocumentElement().normalize();
                // Locate the Tag Name
                nodelist = doc.getElementsByTagName("question");

            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return null;

        }

        @Override
        protected void onPostExecute(Void args) {

            String title = null;
            String link = null;
            String hash = null;
            String date = null;
            items = new ArrayList<XmlItem>();

            for (int temp = 0; temp < nodelist.getLength(); temp++) {
                Node nNode = nodelist.item(temp);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;
                    // Set the texts into TextViews from item nodes

                    if (getNode("PubDate", eElement).endsWith("3"))
                        break;

                    //get hash
                    hash = getNode("ministryName", eElement);

                   /* textview.setText(textview.getText() + "# : "
                            + hash + "\n" + "\n");
*/
                    //get title
                    title = getNode("subject", eElement);

                  /*  textview.setText(textview.getText() + "Title : "
                            + title + "\n" + "\n");*/

                    //get link
                    link = getNode("Link", eElement);
                  /*  textview.setText(textview.getText() + "Link : "
                            + link + "\n" + "\n");*/

                    // Get the date
                    date = getNode("PubDate", eElement);
                   /* textview.setText(textview.getText() + "Date : "
                            + date + "\n" + "\n" + "\n"
                            + "\n");*/
                    XmlItem item = new XmlItem(title, link, hash, date);

                    items.add(item);

                }
            }
            // Close progressbar
            listView.setAdapter(new XmlAdapter(getActivity(), items));
            pDialog.dismiss();
        }
    }

    // getNode function
    private static String getNode(String sTag, Element eElement) {
        NodeList nlList = eElement.getElementsByTagName(sTag).item(0)
                .getChildNodes();
        Node nValue = (Node) nlList.item(0);
        return nValue.getNodeValue();
    }
}
