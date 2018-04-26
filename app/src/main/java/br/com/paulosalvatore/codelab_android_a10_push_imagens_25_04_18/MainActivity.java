package br.com.paulosalvatore.codelab_android_a10_push_imagens_25_04_18;

import android.*;
import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_READ_STORAGE = 1;
    private static final int RESULT_LOAD_IMAGE = 1;
    private NotificationManager notificationManager;
    private final int NOTIFY_ID = 1000;
    private long[] vibracao = new long[]{100, 200, 300, 400, 500, 400, 300, 200, 100};

    private Button btCarregarImagem;
    private ImageView ivImagem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btCarregarImagem = findViewById(R.id.btCarregarImagem);
        ivImagem = findViewById(R.id.ivImagem);

        btCarregarImagem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(
                        MainActivity.this,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(
                            MainActivity.this,
                            new String[] {
                                    Manifest.permission.READ_EXTERNAL_STORAGE
                            },
                            PERMISSION_REQUEST_READ_STORAGE
                    );
                }
                else {
                    carregarImagem();
                }
            }
        });
    }

    private void carregarImagem() {
        Intent intent = new Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        );

        startActivityForResult(intent, RESULT_LOAD_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE &&
                resultCode == RESULT_OK &&
                data != null) {
            Uri imagemSelecionada = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = getContentResolver().query(
                    imagemSelecionada,
                    filePathColumn,
                    null,
                    null,
                    null
            );
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String caminhoImagem = cursor.getString(columnIndex);
            cursor.close();

            ivImagem.setImageBitmap(BitmapFactory.decodeFile(caminhoImagem));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_READ_STORAGE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    carregarImagem();
                }

                break;
        }
    }

    public void pushNotification(View view) {
        criarNotificacao("Título", "Corpo da Notificação Local");
    }

    private void criarNotificacao(String titulo, String corpo) {
        Intent intent;
        PendingIntent pendingIntent;
        NotificationCompat.Builder builder;

        if (notificationManager == null) {
            notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }

        String canalId = "CodeLabPushImagens 1";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String nome = getResources().getString(R.string.default_notification_channel_id);
            String descricao = "CodeLab Push Imagens - Channel";

            NotificationChannel channel = notificationManager.getNotificationChannel(canalId);

            if (channel == null) {
                int importancia = NotificationManager.IMPORTANCE_HIGH;
                channel = new NotificationChannel(canalId, nome, importancia);
                channel.setDescription(descricao);
                channel.enableVibration(true);
                channel.enableLights(true);
                channel.setVibrationPattern(vibracao);
                notificationManager.createNotificationChannel(channel);
            }
        }

        builder = new NotificationCompat.Builder(this, canalId);

        intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        builder.setContentTitle(titulo)
                .setSmallIcon(android.R.drawable.ic_popup_reminder)
                .setContentText(corpo)
                .setDefaults(Notification.DEFAULT_ALL)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setTicker(titulo)
                .setPriority(1)
                .setVibrate(vibracao);

        Notification notification_app = builder.build();
        notificationManager.notify(NOTIFY_ID, notification_app);
    }
}
