package com.example.game_vol1.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.game_vol1.models.POI;
import com.example.game_vol1.models.User;

import java.util.ArrayList;
import java.util.List;

public class GameViewModel extends ViewModel {

    // 1. LiveData обекти: Mutable означава, че можем да ги променяме вътре в този клас.
    // Екранът (Activity) ще има достъп само до четене (LiveData).
    private final MutableLiveData<List<POI>> poiList = new MutableLiveData<>();
    private final MutableLiveData<User> currentUser = new MutableLiveData<>();

    // 2. Конструктор
    public GameViewModel() {
        // Инициализираме празен списък първоначално
        poiList.setValue(new ArrayList<>());
    }

    // 3. Getters за UI (Екранът ще "наблюдава" тези методи)
    public LiveData<List<POI>> getPoiList() {
        return poiList;
    }

    public LiveData<User> getCurrentUser() {
        return currentUser;
    }

    // 4. Бизнес Логика и Действия

    // Този метод симулира зареждане на данни (по-късно ще го свържем с Firebase)
    public void loadMockData() {
        List<POI> mockData = new ArrayList<>();
        // Създаваме примерен обект, за да можем да тестваме картата по-късно
        mockData.add(new POI(
                "1",
                "Античен театър Пловдив",
                "Един от най-добре запазените антични театри в света.",
                42.1466,
                24.7510,
                "url_to_3d_model",
                "Траки"
        ));

        poiList.setValue(mockData); // Това автоматично ще обнови картата на екрана!
    }

    // Метод за добавяне на точки към текущия играч при завършен AR Куест
    public void addPointsToUser(int points) {
        User user = currentUser.getValue();
        if (user != null) {
            user.addPoints(points); // Използваме метода от нашия Model
            currentUser.setValue(user); // Уведомяваме UI, че данните са променени
        }
    }
    // Временен метод за създаване на тестов играч
    public void initDummyUser() {
        if (currentUser.getValue() == null) {
            User dummyUser = new User("1", "Търсач_01", "test@test.com", 0, "Траки");
            currentUser.setValue(dummyUser);
        }
    }
}