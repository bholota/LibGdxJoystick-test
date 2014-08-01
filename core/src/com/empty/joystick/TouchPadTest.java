package com.empty.joystick;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

public class TouchPadTest extends ApplicationAdapter {

    private PerspectiveCamera mainCamera;
    private OrthographicCamera uiCamera;
    private ExtendViewport viewport;
    private ExtendViewport uiViewport;
    private Stage stage;

    private SpriteBatch batch;
    private ModelBatch modelBatch;
    private Environment environment;

    private Touchpad touchpad;
    private Touchpad.TouchpadStyle touchpadStyle;
    private Skin touchpadSkin;

    private Drawable touchBackground;
    private Drawable touchKnob;

    private Model model, floorModel;
    private ModelInstance modelInstance, floorInstance;

    private float blockSpeed;
    private Vector3 lastPos;

    @Override
    public void create() {

        batch = new SpriteBatch();
        modelBatch = new ModelBatch();

        setupMainCamera();
        setupUiCamera();
        setupUi();

        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));

        ModelBuilder modelBuilder = new ModelBuilder();

        model = modelBuilder.createBox(4f, 4f, 4f, new Material(ColorAttribute.createDiffuse(Color.GREEN)),
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);

        floorModel = modelBuilder.createBox(
                100f, 0.1f, 100f,
                new Material(ColorAttribute.createDiffuse(Color.DARK_GRAY)),
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);

        modelInstance = new ModelInstance(model);
        floorInstance = new ModelInstance(floorModel);

        modelInstance.transform.translate(0.0f, 2f, 0f);

        blockSpeed = 5;
        lastPos = new Vector3(0, 2f, 0);
    }

    @Override
    public void dispose() {
        modelBatch.dispose();
        model.dispose();
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0.294f, 0.294f, 0.294f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        mainCamera.update();
        uiCamera.update();

        float touchpadX = touchpad.getKnobPercentX();// * blockSpeed * Gdx.graphics.getDeltaTime();
        float touchpadY = touchpad.getKnobPercentY();// * blockSpeed * Gdx.graphics.getDeltaTime();

        Vector3 joystickDirection = new Vector3(touchpadX, 0, touchpadY);
        Vector3 translation = new Vector3(touchpadX * blockSpeed * Gdx.graphics.getDeltaTime(), 0, -1 * touchpadY * blockSpeed * Gdx.graphics.getDeltaTime());
        lastPos.add(translation);

        if(!joystickDirection.equals(Vector3.Zero) && !lastPos.equals(Vector3.Zero)) {
            modelInstance.transform.setToLookAt(joystickDirection, Vector3.Y).setTranslation(lastPos);
        }

        //camera user tracking
        //mainCamera.position.set(new Vector3(lastPos.x, 20f, lastPos.z + 5));

        modelBatch.begin(mainCamera);
        modelBatch.render(floorInstance, environment);
        modelBatch.render(modelInstance, environment);
        modelBatch.end();

        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        viewport.update(width, height);
        uiViewport.update(width, height);
    }

    protected void setupMainCamera() {
        mainCamera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        mainCamera.position.set(0f, 20f, 10f);
        mainCamera.near = 1f;
        mainCamera.far = 300f;
        mainCamera.lookAt(0, 0, 0);
        mainCamera.update();
        viewport = new ExtendViewport(800, 600, mainCamera);
    }

    protected void setupUiCamera() {
        float aspectRatio = (float) Gdx.graphics.getWidth() / (float) Gdx.graphics.getHeight();
        uiCamera = new OrthographicCamera();
        uiCamera.setToOrtho(false, 10f * aspectRatio, 10f);
        uiViewport = new ExtendViewport(800, 600, uiCamera);
    }

    protected void setupUi() {
        touchpadSkin = new Skin();
        touchpadSkin.add("touchBackground", new Texture("data/touchBackground.png"));
        touchpadSkin.add("touchKnob", new Texture("data/touchKnob.png"));
        touchpadStyle = new Touchpad.TouchpadStyle();
        touchBackground = touchpadSkin.getDrawable("touchBackground");
        touchKnob = touchpadSkin.getDrawable("touchKnob");

        touchpadStyle.background = touchBackground;
        touchpadStyle.knob = touchKnob;

        touchpad = new Touchpad(10, touchpadStyle);
        touchpad.setBounds(15, 15, 200, 200);

        stage = new Stage(uiViewport, batch);
        stage.addActor(touchpad);

        Gdx.input.setInputProcessor(stage);
    }
}