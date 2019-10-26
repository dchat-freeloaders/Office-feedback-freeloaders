package com.github.iauglov.service;

import com.github.iauglov.model.Action;
import static com.github.iauglov.model.Action.ADMIN;
import static com.github.iauglov.model.Action.ADMIN_EVENTS;
import static com.github.iauglov.model.Action.ADMIN_EVENT_CREATE;
import static com.github.iauglov.model.Action.ADMIN_EVENT_DELETE;
import static com.github.iauglov.model.Action.ADMIN_EVENT_DELETE_CONFIRMATION;
import static com.github.iauglov.model.Action.ADMIN_EVENT_EDIT;
import static com.github.iauglov.model.Action.ADMIN_EVENT_EDIT_CONFIRMATION;
import static com.github.iauglov.model.Action.ADMIN_EVENT_FEEDBACKS;
import static com.github.iauglov.model.Action.ADMIN_EVENT_FEEDBACKS_CONFIRMATION;
import static com.github.iauglov.model.Action.ADMIN_EVENT_LIST;
import static com.github.iauglov.model.Action.USER_FEEDBACK_CREATE;
import static com.github.iauglov.model.Action.USER_FEEDBACK_CREATE_CONFIRMATION_FIRST_STEP;
import static com.github.iauglov.model.Action.USER_FEEDBACK_CREATE_CONFIRMATION_SECOND_STEP;
import static com.github.iauglov.model.Action.USER_START;
import com.github.iauglov.persistence.Event;
import com.github.iauglov.persistence.EventRepository;
import com.github.iauglov.persistence.FeedBack;
import com.github.iauglov.persistence.FeedBackRepository;
import com.github.iauglov.persistence.InternalUser;
import com.github.iauglov.persistence.UserRepository;
import im.dlg.botsdk.Bot;
import im.dlg.botsdk.domain.InteractiveEvent;
import im.dlg.botsdk.domain.interactive.InteractiveAction;
import static im.dlg.botsdk.domain.interactive.InteractiveAction.Style.DANGER;
import im.dlg.botsdk.domain.interactive.InteractiveButton;
import im.dlg.botsdk.domain.interactive.InteractiveGroup;
import im.dlg.botsdk.domain.interactive.InteractiveSelect;
import im.dlg.botsdk.domain.interactive.InteractiveSelectOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class InteractiveProcessor {

    private final Bot bot;
    private final CrudCache crudCache;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final FeedBackRepository feedBackRepository;

    public void process(InteractiveEvent event) {
        String id = event.getId().toUpperCase();
        if (Action.canProcess(id)) {
            switch (Action.valueOf(id)) {
                case ADMIN: {
                    processAdmin(event, false);
                    break;
                }
                case ADMIN_EVENTS: {
                    processAdminEvents(event);
                    break;
                }
                case ADMIN_EVENT_LIST: {
                    processAdminEventList(event);
                    break;
                }
                case ADMIN_EVENT_FEEDBACKS: {
                    processAdminEventFeedbacks(event);
                    break;
                }
                case ADMIN_EVENT_FEEDBACKS_CONFIRMATION: {
                    processAdminEventFeedbacksConfirmation(event);
                    break;
                }
                case ADMIN_EVENT_EDIT: {
                    processAdminEventEdit(event);
                    break;
                }
                case ADMIN_EVENT_EDIT_CONFIRMATION: {
                    processAdminEventEditConfirmation(event);
                    break;
                }
                case ADMIN_EVENT_DELETE: {
                    processAdminEventDelete(event);
                    break;
                }
                case ADMIN_EVENT_DELETE_CONFIRMATION: {
                    processAdminEventDeleteConfirmation(event);
                }
                case ADMIN_EVENT_CREATE: {
                    processAdminEventCreate(event);
                    break;
                }
                case USER_START: {
                    processStart(event, false);
                    break;
                }
                case USER_FEEDBACK_CREATE: {
                    processUserFeedbackCreate(event);
                    break;
                }
                case USER_FEEDBACK_CREATE_CONFIRMATION_FIRST_STEP: {
                    processUserFeedbackCreateConfirmationFirstStep(event);
                    break;
                }
                case USER_FEEDBACK_CREATE_CONFIRMATION_SECOND_STEP: {
                    processUserFeedbackCreateConfirmationSecondStep(event);
                    break;
                }
                default: {
                    unknownAction(event);
                }
            }
        } else {
            unknownAction(event);
        }
        System.out.println(event);
    }

    private void processUserFeedbackCreateConfirmationSecondStep(InteractiveEvent event) {
        int satisfactionLevel = Integer.parseInt(event.getValue());

        crudCache.feedBackMap.get(event.getPeer().getId()).setSatisfactionLevel(satisfactionLevel);

        bot.messaging().sendText(event.getPeer(), "Опишите ваши впечатления о событии в чате.");
    }

    private void processUserFeedbackCreate(InteractiveEvent event) {
        List<InteractiveSelectOption> selectOptions = new ArrayList<>();

        eventRepository.findAll().forEach(internalEvent -> {
            selectOptions.add(new InteractiveSelectOption(internalEvent.getId().toString(), internalEvent.getName()));
        });

        InteractiveSelect interactiveSelect = new InteractiveSelect("Выбрать...", "Выбрать...", selectOptions);

        ArrayList<InteractiveAction> actions = new ArrayList<>();

        actions.add(new InteractiveAction(USER_START.asId(), new InteractiveButton(USER_START.asId(), USER_START.getLabel())));
        actions.add(new InteractiveAction(USER_FEEDBACK_CREATE_CONFIRMATION_FIRST_STEP.asId(), interactiveSelect));

        InteractiveGroup group = new InteractiveGroup("Создание фидбека", "Выберите событие, для которого вы хотите оставить фидбек.", actions);

        bot.interactiveApi().update(event.getMid(), group);
    }

    private void processUserFeedbackCreateConfirmationFirstStep(InteractiveEvent event) {
        int eventId = Integer.parseInt(event.getValue());

        Optional<Event> optionalEvent = eventRepository.findById(eventId);

        if (!optionalEvent.isPresent()) {
            bot.messaging().sendText(event.getPeer(), "Событие не найдено, попробуйте снова.").thenAccept(uuid -> {
                processAdmin(event, true);
            });
            return;
        }

        Optional<InternalUser> userOptional = userRepository.findById(event.getPeer().getId());

        if (!userOptional.isPresent()) {
            bot.messaging().sendText(event.getPeer(), "Пользователь не найден, попробуйте снова.").thenAccept(uuid -> {
                processAdmin(event, true);
            });
            return;
        }

        FeedBack feedBack = new FeedBack();
        feedBack.setEvent(optionalEvent.get());
        feedBack.setUser(userOptional.get());

        crudCache.feedBackMap.put(event.getPeer().getId(), feedBack);

        List<InteractiveAction> actions = new ArrayList<>();

        actions.add(new InteractiveAction(USER_FEEDBACK_CREATE_CONFIRMATION_SECOND_STEP.asId(), new InteractiveButton("1", "1")));
        actions.add(new InteractiveAction(USER_FEEDBACK_CREATE_CONFIRMATION_SECOND_STEP.asId(), new InteractiveButton("2", "2")));
        actions.add(new InteractiveAction(USER_FEEDBACK_CREATE_CONFIRMATION_SECOND_STEP.asId(), new InteractiveButton("3", "3")));
        actions.add(new InteractiveAction(USER_FEEDBACK_CREATE_CONFIRMATION_SECOND_STEP.asId(), new InteractiveButton("4", "4")));
        actions.add(new InteractiveAction(USER_FEEDBACK_CREATE_CONFIRMATION_SECOND_STEP.asId(), new InteractiveButton("5", "5")));

        InteractiveGroup group = new InteractiveGroup("Создание фидбека", "Оцените событие от 1 до 5.", actions);

        bot.interactiveApi().update(event.getMid(), group);
    }

    private void processStart(InteractiveEvent event, boolean openNew) {
        Integer userId = event.getPeer().getId();

        InternalUser internalUser = userRepository.findById(userId).get();

        List<InteractiveAction> actions = new ArrayList<>();

        actions.add(new InteractiveAction(USER_FEEDBACK_CREATE.asId(), new InteractiveButton(USER_FEEDBACK_CREATE.asId(), USER_FEEDBACK_CREATE.getLabel())));

        if (internalUser.isAdmin()) {
            actions.add(new InteractiveAction(ADMIN.asId(), new InteractiveButton(ADMIN.asId(), ADMIN.getLabel())));
        }

        InteractiveGroup group = new InteractiveGroup("Feedback bot", String.format("Здравствуйте, %s, хотите оставить фидбек о прошедшем событии?", internalUser.getName()), actions);

        if (openNew) {
            bot.interactiveApi().send(event.getPeer(), group);
        } else {
            bot.interactiveApi().update(event.getMid(), group);
        }
    }

    private void processAdminEventCreate(InteractiveEvent event) {
        int userId = event.getPeer().getId();

        clearAllFor(userId);

        crudCache.eventCreatingMap.add(userId);

        bot.messaging().sendText(event.getPeer(), "Введите название события, на которое можно будет оставить фидбек.");
    }

    private void processAdminEventDeleteConfirmation(InteractiveEvent event) {
        Integer eventId = Integer.valueOf(event.getValue());

        if (eventRepository.existsById(eventId)) {
            eventRepository.deleteById(eventId);
            bot.messaging().delete(event.getMid());
            bot.messaging().sendText(event.getPeer(), "Событие успешно удалено").thenAccept(uuid -> {
                processAdmin(event, true);
            });
        } else {
            bot.messaging().delete(event.getMid());
            bot.messaging().sendText(event.getPeer(), "Вы пытаетесь удалить уже удаленное событие.").thenAccept(uuid -> {
                processAdmin(event, true);
            });
        }
    }

    private void processAdminEventDelete(InteractiveEvent event) {
        List<InteractiveSelectOption> selectOptions = new ArrayList<>();

        eventRepository.findAll().forEach(internalEvent -> {
            selectOptions.add(new InteractiveSelectOption(internalEvent.getId().toString(), internalEvent.getName()));
        });

        InteractiveSelect interactiveSelect = new InteractiveSelect("Выбрать...", "Выбрать...", selectOptions);

        ArrayList<InteractiveAction> actions = new ArrayList<>();

        actions.add(new InteractiveAction(ADMIN_EVENTS.asId(), new InteractiveButton(ADMIN_EVENTS.asId(), ADMIN_EVENTS.getLabel())));
        actions.add(new InteractiveAction(ADMIN_EVENT_DELETE_CONFIRMATION.asId(), interactiveSelect));

        InteractiveGroup group = new InteractiveGroup("Удаление событий", "Выберите событие, которое вы хотите удалить.", actions);

        bot.interactiveApi().update(event.getMid(), group);
    }

    private void processAdminEventEditConfirmation(InteractiveEvent event) {
        int eventId = Integer.parseInt(event.getValue());

        clearAllFor(event.getPeer().getId());

        crudCache.eventEditingMap.put(event.getPeer().getId(), eventId);

        bot.messaging().sendText(event.getPeer(), "Введите новое название для события");
    }

    private void clearAllFor(int peerId) {
        crudCache.eventCreatingMap.remove(peerId);
        crudCache.eventEditingMap.remove(peerId);
        crudCache.feedBackMap.remove(peerId);
    }

    private void processAdminEventEdit(InteractiveEvent event) {
        List<InteractiveSelectOption> selectOptions = new ArrayList<>();

        eventRepository.findAll().forEach(internalEvent -> {
            selectOptions.add(new InteractiveSelectOption(internalEvent.getId().toString(), internalEvent.getName()));
        });

        InteractiveSelect interactiveSelect = new InteractiveSelect("Выбрать...", "Выбрать...", selectOptions);

        ArrayList<InteractiveAction> actions = new ArrayList<>();

        actions.add(new InteractiveAction(ADMIN_EVENTS.asId(), new InteractiveButton(ADMIN_EVENTS.asId(), ADMIN_EVENTS.getLabel())));
        actions.add(new InteractiveAction(ADMIN_EVENT_EDIT_CONFIRMATION.asId(), interactiveSelect));

        InteractiveGroup group = new InteractiveGroup("Редактирование событий", "Выберите событие, которому хотите отредактировать название.", actions);

        bot.interactiveApi().update(event.getMid(), group);
    }

    private void processAdminEventFeedbacksConfirmation(InteractiveEvent event) {
        int eventId = Integer.parseInt(event.getValue());

        List<FeedBack> feedbacks = feedBackRepository.findAllByEventId(eventId);

        if (feedbacks.size() == 0) {
            bot.messaging().sendText(event.getPeer(), "Отзывов нет.");
        }

        StringBuilder stringBuilder = new StringBuilder();

        feedbacks.forEach(feedBack -> {
            stringBuilder
                    .append("ID: ").append(feedBack.getId())
                    .append(", Оценивший: ").append(feedBack.getUser().getName())
                    .append(", Оценка: ").append(feedBack.getSatisfactionLevel())
                    .append(", Текст: ").append(feedBack.getText())
                    .append(".")
                    .append("\n");
        });

        stringBuilder.deleteCharAt(stringBuilder.length() - 1);

        bot.messaging().sendText(event.getPeer(), stringBuilder.toString()).thenAccept(uuid -> {
            processAdmin(event, true);
        });
    }

    private void processAdminEventFeedbacks(InteractiveEvent event) {
        List<InteractiveSelectOption> selectOptions = new ArrayList<>();

        eventRepository.findAll().forEach(internalEvent -> {
            selectOptions.add(new InteractiveSelectOption(internalEvent.getId().toString(), internalEvent.getName()));
        });

        InteractiveSelect interactiveSelect = new InteractiveSelect("Выбрать...", "Выбрать...", selectOptions);

        ArrayList<InteractiveAction> actions = new ArrayList<>();

        actions.add(new InteractiveAction(ADMIN_EVENTS.asId(), new InteractiveButton(ADMIN_EVENTS.asId(), ADMIN_EVENTS.getLabel())));
        actions.add(new InteractiveAction(ADMIN_EVENT_FEEDBACKS_CONFIRMATION.asId(), interactiveSelect));

        InteractiveGroup group = new InteractiveGroup("Получение фидбеков", "Выберите событие, по которому хотите получить фидбеки.", actions);

        bot.interactiveApi().update(event.getMid(), group);
    }

    private void processAdminEventList(InteractiveEvent event) {
        List<Event> events = eventRepository.findAll();

        if (events.size() == 0) {
            bot.messaging().sendText(event.getPeer(), "Событий нет.");
        }

        StringBuilder stringBuilder = new StringBuilder();

        events.forEach(internalEvent -> {
            List<FeedBack> feedBacks = feedBackRepository.findAllByEventId(internalEvent.getId());
            int sum = 0;

            for (FeedBack feedBack : feedBacks) {
                sum += feedBack.getSatisfactionLevel();
            }

            String average = String.format("%.2f", (double) sum / feedBacks.size());

            stringBuilder
                    .append("ID: ").append(internalEvent.getId())
                    .append(", Название события: ").append(internalEvent.getName())
                    .append(", Средняя оценка: ").append(average)
                    .append(".")
                    .append("\n");
        });

        stringBuilder.deleteCharAt(stringBuilder.length() - 1);

        bot.messaging().sendText(event.getPeer(), stringBuilder.toString()).thenAccept(uuid -> {
            processAdmin(event, true);
        });
    }

    private void processAdminEvents(InteractiveEvent event) {
        List<InteractiveAction> actions = new ArrayList<>();

        actions.add(new InteractiveAction(ADMIN.asId(), new InteractiveButton(ADMIN.asId(), ADMIN.getLabel())));
        actions.add(new InteractiveAction(ADMIN_EVENT_LIST.asId(), new InteractiveButton(ADMIN_EVENT_LIST.asId(), ADMIN_EVENT_LIST.getLabel())));
        actions.add(new InteractiveAction(ADMIN_EVENT_FEEDBACKS.asId(), new InteractiveButton(ADMIN_EVENT_FEEDBACKS.asId(), ADMIN_EVENT_FEEDBACKS.getLabel())));
        actions.add(new InteractiveAction(ADMIN_EVENT_CREATE.asId(), new InteractiveButton(ADMIN_EVENT_CREATE.asId(), ADMIN_EVENT_CREATE.getLabel())));
        actions.add(new InteractiveAction(ADMIN_EVENT_EDIT.asId(), new InteractiveButton(ADMIN_EVENT_EDIT.asId(), ADMIN_EVENT_EDIT.getLabel())));
        actions.add(new InteractiveAction(ADMIN_EVENT_DELETE.asId(), DANGER, new InteractiveButton(ADMIN_EVENT_DELETE.asId(), ADMIN_EVENT_DELETE.getLabel()), null));

        InteractiveGroup group = new InteractiveGroup("События", "Операции с событиями.", actions);

        bot.interactiveApi().update(event.getMid(), group);
    }

    private void processAdmin(InteractiveEvent event, boolean createNew) {
        List<InteractiveAction> actions = new ArrayList<>();

        actions.add(new InteractiveAction(USER_START.asId(), new InteractiveButton(USER_START.asId(), USER_START.getLabel())));
        actions.add(new InteractiveAction(ADMIN_EVENTS.asId(), new InteractiveButton(ADMIN_EVENTS.asId(), ADMIN_EVENTS.getLabel())));
//        actions.add(new InteractiveAction(QUESTIONS.asId(), new InteractiveButton(QUESTIONS.asId(), QUESTIONS.getLabel())));
//        actions.add(new InteractiveAction(ANSWERS.asId(), new InteractiveButton(ANSWERS.asId(), ANSWERS.getLabel())));

        InteractiveGroup group = new InteractiveGroup("Админ-панель", "Выберите группу действий.", actions);

        if (createNew) {
            bot.interactiveApi().send(event.getPeer(), group);
        } else {
            bot.interactiveApi().update(event.getMid(), group);
        }
    }

    private void unknownAction(InteractiveEvent interactiveEvent) {
        String text = "Действие в разработке или не зарегистрировано.\nВведите /help для отображения списка команд.";
        bot.messaging().sendText(interactiveEvent.getPeer(), text);
    }

}
