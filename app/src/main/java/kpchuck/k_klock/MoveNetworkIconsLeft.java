package kpchuck.k_klock;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;
import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;

import kpchuck.k_klock.Utils.FileHelper;
import kpchuck.k_klock.Utils.PrefUtils;

/**
 * Created by Karol Przestrzelski on 04/09/2017.
 */

public class MoveNetworkIconsLeft {

    private Context context;

    String rootFolder = Environment.getExternalStorageDirectory() + "/K-Klock";
    String slash = "/";
    File mergerFolder = new File(rootFolder + slash + "temp2" + slash + "merge");
    String rootApkPath = mergerFolder.getAbsolutePath() + "/assets/overlays/com.android.systemui";
    String systemicons = "system_icons.xml";
    String statusbar = "status_bar.xml";
    String romName;
    boolean toMoveLeft;
    boolean toMinit;


    public MoveNetworkIconsLeft(Context context){
        this.context=context;
        getPref();
        if (toMoveLeft || toMinit) {
            if (!romName.equals(context.getString(R.string.otherRomsBeta)))
                copySystemIconsAssets("systemicons", romName);
            else copyFromUserInput();

            //
            editSystemIcons();
            if (toMoveLeft)editStatusBar();
        }

    }

    private void copyFromUserInput(){
        try{
            String userInputPath = rootFolder + "/userInput/";
            File file = new File(userInputPath + systemicons);
            FileUtils.copyFileToDirectory(file, new File(rootApkPath + "/res/layout"));

        }catch (IOException e){
            Log.e("klock", e.getMessage());
        }
    }

    private void editStatusBar(){
        File rootFile = new File(rootApkPath);
        File[] files = rootFile.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isDirectory();
            }
        });
        for(File s: files){
            if (!new File(s.getAbsolutePath() + slash + "layout").exists()) continue;
            File file = new File(s.getAbsolutePath() + slash + "layout/" +  statusbar);
            try {
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                Document doc = db.parse(file);

                Element resources = doc.getDocumentElement();
                NodeList list = resources.getElementsByTagName("LinearLayout");
                Element layout = null;
                for (int i=0;i<list.getLength();i++){
                    Element layouttemp = (Element) list.item(i);
                    Attr attr = layouttemp.getAttributeNode("android:id");
                    if (attr.getValue().equals("@*com.android.systemui:id/status_bar_contents")) {
                        Log.d("klock", "found statusbar contentd layout");
                        layout = layouttemp;
                    }

                    //  else Log.d("klock", "statusbarcontents layout was null");
                }

                Element insertBeforeElement;

                Node firstNode = layout.getFirstChild();
                while (firstNode.getNodeType() != Node.ELEMENT_NODE) firstNode = firstNode.getNextSibling();

                Element firstElement = (Element) firstNode;
                String firstTag = firstElement.getTagName();

                Log.d("klock", "First Tag is "+ firstTag);
                if (firstTag.equals("LinearLayout") || firstTag.equals("TextClock")){
                    Node nextElement = firstElement.getNextSibling();
                    while (nextElement.getNodeType() != Node.ELEMENT_NODE){
                        nextElement = nextElement.getNextSibling();
                    }
                    insertBeforeElement = (Element) nextElement;
                }
                else {
                    insertBeforeElement = firstElement;
                }
                //Add layout width attribute
                if (file.getAbsolutePath().contains("Center") || file.getAbsolutePath().contains("res")) {

                    if (layout.getAttributes().getNamedItem("android:paddingEnd") == null) {
                        layout.setAttribute("android:paddingEnd", "8.5dip");
                    } else {
                        layout.removeAttribute("android:paddingEnd");
                        layout.setAttribute("android:paddingEnd", "8.5dip");
                    }
                }


                Element toInclude = doc.createElement("include");
                toInclude.setAttribute("android:layout_width", "wrap_content");
                toInclude.setAttribute("android:layout_height", "fill_parent");
                toInclude.setAttribute("android:layout_marginStart", "2.5dip");
                toInclude.setAttribute("layout", "@*com.android.systemui:layout/signal_cluster_view");
                toInclude.setAttribute("android:gravity", "center_vertical");

                layout.insertBefore(toInclude, insertBeforeElement);

                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                DOMSource source = new DOMSource(doc);

                StreamResult result = new StreamResult(file);
                transformer.transform(source, result);
            }catch (Exception e){
                Log.e("klock", e.getMessage());

            }

        }

    }

    private void editSystemIcons(){
        File sysicons = new File(rootApkPath + "/res/layout/" + systemicons);
        sysicons.mkdirs();
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(sysicons);

            if (toMinit){
                Element rootElement = doc.getDocumentElement();
                NodeList list = rootElement.getElementsByTagName("com.android.systemui.BatteryMeterView");
                Element battery = (Element) list.item(0);
                battery.removeAttribute("android:layout_width");
                battery.removeAttribute("android:layout_height");
                battery.setAttribute("android:layout_width", "0.0dip");
                battery.setAttribute("android:layout_height", "0.0dip");

                Element minitmod = doc.createElement("com.android.systemui.statusbar.policy.MinitBattery");
                minitmod.setAttribute("android:layout_width", "wrap_content");
                minitmod.setAttribute("android:layout_height", "wrap_content");
                minitmod.setAttribute("android:layout_marginEnd", "7.0dip");

                rootElement.insertBefore(minitmod, battery);

            }

            if(toMoveLeft) {
                Element rootElement = doc.getDocumentElement();
                NodeList list = rootElement.getElementsByTagName("include");
                Element includeElement = (Element) list.item(0);

                rootElement.removeChild(includeElement);
            }

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);

            StreamResult result = new StreamResult(sysicons);
            transformer.transform(source, result);

            replaceStuffInXml(sysicons.getAbsolutePath(), "@", "@*com.android.systemui:");
        }catch (Exception e){
            Log.e("klock", e.getMessage());

        }

    }

    public void replaceStuffInXml(String xml, String old, String news){
        try {
            File file = new File(xml);

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(file);

            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));
            String output = writer.getBuffer().toString().replaceAll("\n|\r", "");
            output = output.replace(old, news);
            doc = stringToDom(output);
            doc.normalizeDocument();

            DOMSource source = new DOMSource(doc);

            if (file.exists()) FileUtils.forceDelete(file);

            StreamResult result = new StreamResult(file);
            transformer.transform(source, result);

        }catch (Exception e){Log.e("klock", e.getMessage());

        }
    }

    public static Document stringToDom(String xmlSource)
            throws SAXException, ParserConfigurationException, IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new InputSource(new StringReader(xmlSource)));
    }


    private void getPref(){
        PrefUtils prefUtils = new PrefUtils(context);
        String rom = prefUtils.getString("selectedRom", context.getString(R.string.chooseRom));
        this.romName=rom;
        this.toMoveLeft=prefUtils.getBool("moveLeftPref");
        this.toMinit=prefUtils.getBool("minitPref");
    }

    private void copySystemIconsAssets(String assetDir, String whichString) {
        AssetManager assetManager = context.getAssets();
        String[] files = null;
        try {
            files = assetManager.list(assetDir);
        } catch (IOException e) {
            android.util.Log.e("tag", "Failed to get asset file list.", e);

        }
        if (files != null) for (String filename : files) {
            if(filename.equals(whichString)){
                java.io.InputStream in = null;
                java.io.OutputStream out = null;
                try {
                    in = assetManager.open(assetDir + slash + filename + slash + systemicons);
                    FileHelper fileHelper = new FileHelper();
                    fileHelper.newFolder(rootApkPath + "/res/layout");
                    File outFile = new File(rootApkPath + "/res/layout/" + systemicons);
                    out = new java.io.FileOutputStream(outFile);
                    copyFile(in, out);
                } catch(IOException e) {
                    android.util.Log.e("tag", "Failed to copy asset file: " + filename, e);
                }
                finally {
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException e) {
                            // NOOP
                        }
                    }
                    if (out != null) {
                        try {
                            out.close();
                        } catch (IOException e) {
                            // NOOP
                        }
                    }}
            }
        }
    }

    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }
    }
}
