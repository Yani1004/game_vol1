package com.example.game_vol1;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.ar.core.Anchor;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

public class ARQuestActivity extends AppCompatActivity {

    private ArFragment arFragment;
    private ViewRenderable floatingButtonRenderable; // Нашият 3D Бутон

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_arquest);

        Button btnBackToMap = findViewById(R.id.btnBackToMap);
        btnBackToMap.setOnClickListener(v -> finish());

        // 1. Взимаме AR фрагмента от екрана
        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.arFragment);

        // 2. Подготвяме 3D Холограмата (зареждаме XML-а, който създадохме)
        buildFloatingButton();

        // 3. Какво се случва, когато играчът докосне РАЗПОЗНАТА ПОВЪРХНОСТ (пода)?
        arFragment.setOnTapArPlaneListener((hitResult, plane, motionEvent) -> {

            // Ако бутонът все още не е заредил в паметта, излизаме
            if (floatingButtonRenderable == null) {
                return;
            }

            // Създаваме "Котва" (Anchor) на мястото, където потребителят е докоснал в реалния свят
            Anchor anchor = hitResult.createAnchor();
            AnchorNode anchorNode = new AnchorNode(anchor);
            anchorNode.setParent(arFragment.getArSceneView().getScene());

            // Създаваме възел (Node), който държи нашия 3D обект и може да се върти/уголемява
            TransformableNode artifactNode = new TransformableNode(arFragment.getTransformationSystem());
            artifactNode.setParent(anchorNode);
            artifactNode.setRenderable(floatingButtonRenderable); // Закачаме холограмата

            // Караме го да се появи малко по-високо над пода (0.5 метра нагоре)
            artifactNode.setLocalPosition(new Vector3(0f, 0.5f, 0f));
            artifactNode.select();

            // 4. ТУК Е САМОТО ЧЕКИРАНЕ: Какво става, когато докоснем самата 3D Холограма?
            artifactNode.setOnTapListener((hitTestResult, motionEvent1) -> {

                String wonArtifact = getIntent().getStringExtra("ARTIFACT_NAME");
                int points = getIntent().getIntExtra("POINTS", 50);

                Toast.makeText(this, "🎉 ЧЕСТИТО! Ти откри " + wonArtifact + "! (+" + points + " точки)", Toast.LENGTH_LONG).show();

                anchorNode.removeChild(artifactNode);
                arFragment.getArSceneView().postDelayed(this::finish, 2000);
            });
        });
    }

    // Метод, който компилира нашия 2D XML в 3D обект
    // Метод, който компилира нашия 2D XML в 3D обект и го прави ДИНАМИЧЕН
    private void buildFloatingButton() {

        // 1. Извличаме данните, които Картата ни изпрати
        String artifactName = getIntent().getStringExtra("ARTIFACT_NAME");
        String artifactIcon = getIntent().getStringExtra("ARTIFACT_ICON");

        ViewRenderable.builder()
                .setView(this, R.layout.ar_floating_button)
                .build()
                .thenAccept(renderable -> {
                    this.floatingButtonRenderable = renderable;

                    // 2. Взимаме самия дизайн (View) на холограмата
                    android.view.View view = renderable.getView();

                    // 3. Намираме текстовите полета
                    android.widget.TextView tvIcon = view.findViewById(R.id.tvArtifactIcon);
                    android.widget.TextView tvName = view.findViewById(R.id.tvArtifactName);

                    // 4. Сменяме текста с този, който дойде от Картата!
                    if (artifactIcon != null) tvIcon.setText(artifactIcon);
                    if (artifactName != null) tvName.setText(artifactName);
                })
                .exceptionally(throwable -> {
                    Toast.makeText(this, "Грешка при зареждане на 3D модела", Toast.LENGTH_SHORT).show();
                    return null;
                });
    }
}