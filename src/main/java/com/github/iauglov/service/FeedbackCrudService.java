package com.github.iauglov.service;

import static com.github.iauglov.model.Action.ADMIN;
import static com.github.iauglov.model.Action.ADMIN_EVENTS;
import static com.github.iauglov.model.Action.USER_START;
import com.github.iauglov.persistence.Event;
import com.github.iauglov.persistence.EventRepository;
import com.github.iauglov.persistence.FeedBack;
import com.github.iauglov.persistence.FeedBackRepository;
import com.github.iauglov.persistence.InternalUser;
import com.github.iauglov.persistence.UserRepository;
import im.dlg.botsdk.Bot;
import im.dlg.botsdk.domain.Message;
import im.dlg.botsdk.domain.Peer;
import im.dlg.botsdk.domain.interactive.InteractiveAction;
import im.dlg.botsdk.domain.interactive.InteractiveButton;
import im.dlg.botsdk.domain.interactive.InteractiveGroup;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class FeedbackCrudService {

    private final Bot bot;
    private final CrudCache crudCache;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final FeedBackRepository feedBackRepository;

    public boolean processMessage(Message message) {

        if (crudCache.eventEditingMap.containsKey(message.getPeer().getId())) {
            processEventEditing(message);
            return true;
        }

        if (crudCache.eventCreatingMap.contains(message.getPeer().getId())) {
            processEventCreating(message);
            return true;
        }

        if (crudCache.feedBackMap.containsKey(message.getPeer().getId())) {
            processFeedbackCreating(message);
            return true;
        }

        return false;
    }

    private void processFeedbackCreating(Message message) {
        FeedBack feedBack = crudCache.feedBackMap.remove(message.getPeer().getId());

        feedBack.setText(message.getText());

        feedBackRepository.save(feedBack);

        bot.messaging().sendText(message.getPeer(), "Фидбек успешно отправлен. Спасибо за ваш отзыв!").thenAccept(uuid -> {
            openStart(message.getPeer());
        });
    }

    private void processEventCreating(Message message) {
        crudCache.eventCreatingMap.remove(message.getPeer().getId());

        String eventTitle = message.getText();
        Event event = new Event();
        event.setName(eventTitle);

        eventRepository.save(event);

        bot.messaging().sendText(message.getPeer(), "Событие успешно создано.").thenAccept(uuid -> {
            openInteractiveAdmin(message.getPeer());
        });
    }

    private void processEventEditing(Message message) {
        String eventTitle = message.getText();

        Integer vacationId = crudCache.eventEditingMap.remove(message.getPeer().getId());
        Optional<Event> optionalEvent = eventRepository.findById(vacationId);

        if (!optionalEvent.isPresent()) {
            bot.messaging().sendText(message.getPeer(), "Событие не найдено, попробуйте снова.").thenAccept(uuid -> {
                openInteractiveAdmin(message.getPeer());
            });
            return;
        }

        Event event = optionalEvent.get();
        event.setName(eventTitle);

        eventRepository.save(event);
        bot.messaging().sendText(message.getPeer(), "Событие успешно отредактировано.").thenAccept(uuid -> {
            openInteractiveAdmin(message.getPeer());
        });
    }

    public void openStart(Peer peer) {
        InternalUser user = userRepository.findById(peer.getId()).get();

        List<InteractiveAction> actions = new ArrayList<>();

        actions.add(new InteractiveAction(USER_START.asId(), new InteractiveButton(USER_START.asId(), USER_START.getLabel())));

        if (user.isAdmin()) {
            actions.add(new InteractiveAction(ADMIN.asId(), new InteractiveButton(ADMIN.asId(), ADMIN.getLabel())));
        }

        InteractiveGroup group = new InteractiveGroup("Vacation bot", String.format("Здравствуйте, %s, хотите запланировать отпуск?\n\nПосле планирования отпуска данные автоматически будут отправлены вашему непосредственному начальнику.", user.getName()), actions);

        bot.interactiveApi().send(peer, group);
    }

    private void openInteractiveAdmin(Peer peer) {
        List<InteractiveAction> actions = new ArrayList<>();

        actions.add(new InteractiveAction(ADMIN_EVENTS.asId(), new InteractiveButton(ADMIN_EVENTS.asId(), ADMIN_EVENTS.getLabel())));
//        actions.add(new InteractiveAction(QUESTIONS.asId(), new InteractiveButton(QUESTIONS.asId(), QUESTIONS.getLabel())));
//        actions.add(new InteractiveAction(ANSWERS.asId(), new InteractiveButton(ANSWERS.asId(), ANSWERS.getLabel())));

        InteractiveGroup group = new InteractiveGroup("Админ-панель", "Выберите группу действий.", actions);

        bot.interactiveApi().send(peer, group);
    }

}
