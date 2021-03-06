package kpchuck.k_klock;

/**
 * Created by Karol Przestrzelski on 07/08/2017.
 */
import android.content.Context;
import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class xmlCreation {

    Context context;
    public void putContext(Context context){
        this.context=context;
    }

    public void createTypeA(String xmlFilename, String colorCode){
        try {

           String start = "<?xml version=\"1.0\" encoding=\"utf-8\"?><resources><color name=\"status_bar_clock_color\">";
            String end = "</color></resources>";
            String fullColorXml = start + colorCode + end;
            OtherRomsHandler handler = new OtherRomsHandler(context);

            Document doc = handler.stringToDom(fullColorXml);

            String slash = "/";
            String rootFolder = android.os.Environment.getExternalStorageDirectory() + slash + "K-Klock" + slash + "type1a_"+xmlFilename;
            // write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(rootFolder));

            // Output to console for testing
            // StreamResult result = new StreamResult(System.out);

            transformer.transform(source, result);

        }  catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void createTypeB(String xmlFilename, String formatCode){
        try {

   String start = "<?xml version=\"1.0\" encoding=\"utf-8\"?><resources><string name=\"keyguard_widget_12_hours_format\"> ";
            String middle = " </string><string name=\"keyguard_widget_24_hours_format\"> ";
            String end = " </string></resources>";
            String fullXmlString = start + formatCode + middle + formatCode + end;

            OtherRomsHandler handler = new OtherRomsHandler(context);
            Document doc = handler.stringToDom(fullXmlString);

            String slash = "/";
            String rootFolder = android.os.Environment.getExternalStorageDirectory() + slash + "K-Klock" + slash + "type1b_"+xmlFilename;
            // write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(rootFolder));

            // Output to console for testing
            // StreamResult result = new StreamResult(System.out);

            transformer.transform(source, result);


        } catch (Exception pce) {
            pce.getMessage();
        }

    }

}
