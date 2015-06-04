package com.itcms.onoff_simplifiedui.controllers;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.WindowManager;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VerticalSeekBar;

import com.itcms.onoff_simplifiedui.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lifx.java.android.client.LFXClient;
import lifx.java.android.entities.LFXHSBKColor;
import lifx.java.android.entities.LFXTypes;
import lifx.java.android.light.LFXLight;
import lifx.java.android.light.LFXLight.LFXLightListener;
import lifx.java.android.light.LFXLightCollection;
import lifx.java.android.light.LFXLightCollection.LFXLightCollectionListener;
import lifx.java.android.light.LFXTaggedLightCollection;
import lifx.java.android.network_context.LFXNetworkContext;
import lifx.java.android.network_context.LFXNetworkContext.LFXNetworkContextListener;


/**
 * Created by Dmitriy G. on 02.06.2015.
 */
public class MainActivity extends ActionBarActivity
        implements LFXNetworkContextListener, LFXLightCollectionListener, LFXLightListener {

    private LFXNetworkContext lfxNetworkContext;
    private MulticastLock multicastLock;
    private LFXLightCollection allLights;
    private int lightNumber = 0;
    private HashMap<LFXLight, List<TextView>> lightsStateMap = new HashMap<>();
    private List<LightController> lightControllerList = new ArrayList<>();
    private List<Switch> bulbSwitch       = new ArrayList<>();
    private List<VerticalSeekBar> dimBar  = new ArrayList<>();
    private List<VerticalSeekBar> tempBar = new ArrayList<>();
    private List<ImageButton> labelButton = new ArrayList<>();
    private List<TextView> dimView        = new ArrayList<>();
    private List<TextView> tempView       = new ArrayList<>();
    private List<TextView> labelView      = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.main_activity);

        initUI();

        WifiManager wifiManager = (WifiManager) getSystemService (Context.WIFI_SERVICE);
        multicastLock = wifiManager.createMulticastLock("lifx_control_tag");
        multicastLock.acquire();

        lfxNetworkContext = LFXClient.getSharedInstance(getApplicationContext()).getLocalNetworkContext();
        lfxNetworkContext.connect();
        lfxNetworkContext.addNetworkContextListener(this);

        allLights = lfxNetworkContext.getAllLightsCollection();
        allLights.addLightCollectionListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!lfxNetworkContext.isConnected()) {
            lfxNetworkContext.connect();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        lfxNetworkContext.disconnect();
    }

    private void initUI() {

        bulbSwitch.add((Switch) findViewById(R.id.switch1));
        bulbSwitch.add((Switch) findViewById(R.id.switch2));
        bulbSwitch.add((Switch) findViewById(R.id.switch3));

        dimBar.add((VerticalSeekBar) findViewById(R.id.dim1seek));
        dimBar.add((VerticalSeekBar) findViewById(R.id.dim2seek));
        dimBar.add((VerticalSeekBar) findViewById(R.id.dim3seek));

        tempBar.add((VerticalSeekBar) findViewById(R.id.temp1seek));
        tempBar.add((VerticalSeekBar) findViewById(R.id.temp2seek));
        tempBar.add((VerticalSeekBar) findViewById(R.id.temp3seek));

        labelButton.add((ImageButton) findViewById(R.id.btn_change_label1));
        labelButton.add((ImageButton) findViewById(R.id.btn_change_label2));
        labelButton.add((ImageButton) findViewById(R.id.btn_change_label3));

        dimView.add((TextView) findViewById(R.id.dim1view));
        dimView.add((TextView) findViewById(R.id.dim2view));
        dimView.add((TextView) findViewById(R.id.dim3view));

        tempView.add((TextView) findViewById(R.id.temp1view));
        tempView.add((TextView) findViewById(R.id.temp2view));
        tempView.add((TextView) findViewById(R.id.temp3view));

        labelView.add((TextView) findViewById(R.id.label1));
        labelView.add((TextView) findViewById(R.id.label2));
        labelView.add((TextView) findViewById(R.id.label3));

    }

    @Override
    public void networkContextDidConnect(LFXNetworkContext networkContext) {
        Toast.makeText(this, "Connected to WiFi network", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void networkContextDidDisconnect(LFXNetworkContext networkContext) {
        Toast.makeText(this, "Disconnected from WiFi network", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void networkContextDidAddTaggedLightCollection(LFXNetworkContext networkContext, LFXTaggedLightCollection collection) {

    }

    @Override
    public void networkContextDidRemoveTaggedLightCollection(LFXNetworkContext networkContext, LFXTaggedLightCollection collection) {

    }

    @Override
    public void lightCollectionDidAddLight(LFXLightCollection lightCollection, LFXLight light) {

        if (lightNumber > 2) {
            Toast.makeText(this, "No more switches", Toast.LENGTH_SHORT).show();
            lightCollection.removeLight(light);
            return;
        }

        List<TextView> dataViews = new ArrayList<>();
        dataViews.add(dimView.get(lightNumber));
        dataViews.add(tempView.get(lightNumber));
        dataViews.add(labelView.get(lightNumber));

        lightsStateMap.put(light, dataViews);
        lightControllerList.add(new LightController(
                this,
                light,
                bulbSwitch.get(lightNumber),
                dimBar.get(lightNumber),
                tempBar.get(lightNumber),
                labelButton.get(lightNumber)
        ));

        lightNumber++;

        Toast.makeText(this, "Bulb is added (" + lightNumber + ")", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void lightCollectionDidRemoveLight(LFXLightCollection lightCollection, LFXLight light) {

        lightsStateMap.remove(light);

        lightNumber--;

        Toast.makeText(this, "Bulb is deleted (" + lightNumber + ")", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void lightCollectionDidChangeLabel(LFXLightCollection lightCollection, String label) {

    }

    @Override
    public void lightCollectionDidChangeColor(LFXLightCollection lightCollection, LFXHSBKColor color) {

    }

    @Override
    public void lightCollectionDidChangeFuzzyPowerState(LFXLightCollection lightCollection, LFXTypes.LFXFuzzyPowerState fuzzyPowerState) {

    }

    @Override
    public void lightDidChangeLabel(LFXLight light, String label) {
        TextView labelView = lightsStateMap.get(light).get(2);

        labelView.setText(label);
    }

    @Override
    public void lightDidChangeColor(LFXLight light, LFXHSBKColor color) {
        TextView dimView = lightsStateMap.get(light).get(0);
        TextView tempView = lightsStateMap.get(light).get(1);

        dimView.setText("" + Math.round(color.getBrightness() * 100) + "%");
        tempView.setText("" + color.getKelvin() + "K");
    }

    @Override
    public void lightDidChangePowerState(LFXLight light, LFXTypes.LFXPowerState powerState) {
        //Toast.makeText(this, light.getLabel() + " has changed its power state to " + powerState.toString(), Toast.LENGTH_SHORT).show();
    }
}