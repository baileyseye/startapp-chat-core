# 🚀 Kubernetes — Список команд

> Полное руководство по настройке и работе с Minikube для проекта **startapp-chat-core**

---

## 📋 Основная настройка Kubernetes (с начала до конца)

```bash
# 1. Переход в директорию Kubernetes
cd C:\ProgramData\Microsoft\Windows\Start Menu\Programs\Kubernetes

# 2. Проверка Minikube
minikube

# 3. Запуск Minikube с Docker-драйвером
minikube start --driver=docker

# 4. Открытие дашборда Minikube
minikube dashboard

# 5. Настройка окружения Docker (для Windows CMD)
FOR /f "tokens=*" %i IN ('minikube -p minikube docker-env --shell cmd') DO @%i

# 6. Сборка Docker-образа (точка в конце!)
docker build -t startapp-chat-core:latest .

# 7. Переход в директорию с манифестами Kubernetes
cd C:\Users\user\IdeaProjects\startapp-chat-core\k8s

# 8-11. Применение манифестов (можно по одному или все сразу)
kubectl apply -f secret.yaml
kubectl apply -f configmap.yaml
kubectl apply -f postgres.yaml
kubectl apply -f app.yaml

# Альтернатива: применение всех манифестов из текущей директории
kubectl apply -f .

# 12. Удаление подов (навсякий случай)
kubectl delete pod -l=startapp-chat-core

# 13. Загрузка образа в Minikube
minikube image load startapp-chat-core:latest

# 14. Проверка загрузки образа
minikube image ls | findstr startapp

# 15. Открытие сервиса через Minikube
minikube service startapp-chat-core-service

# 15.1. Альтернатива: Port Forwarding
kubectl port-forward service/startapp-chat-core-service 8080:8080
# Затем открыть в браузере: localhost:8080

# 16. Просмотр логов
kubectl logs -l app=startapp-chat-core --tail=100

# 17. Перезапуск Deployment
kubectl rollout restart deployment/startapp-chat-core

# 18. Редактирование ConfigMap
kubectl edit configmap app-config

# 19. Просмотр значений переменных окружения в ConfigMap
kubectl get configmap app-config -o yaml | findstr SPRING_PROFILES_ACTIVE

# 20. Просмотр логов в реальном времени
kubectl logs -f startapp-chat-core-db7578fcd-5xqpj

# 21. Остановка Minikube
minikube stop
```

---

## 🔄 Пересборка и перепод микросервиса

### Если Minikube ещё не запущен:

```bash
# 1. Переход в директорию Kubernetes(необязательно, по путям в дальнейшем можно сразу переходить на шаг 2)
cd C:\ProgramData\Microsoft\Windows\Start Menu\Programs\Kubernetes

# 2. Проверка Minikube
minikube

# 3. Запуск Minikube
minikube start --driver=docker
```

### Если Minikube уже работает:

```bash
# 1. Пересборка Docker-образа
docker build -t startapp-chat-core:latest .

# 2. Перезапуск Deployment
kubectl rollout restart deployment/startapp-chat-core

#3. Проверяем поды (в конце добавить можно -w чтобы реалтайм смотреть)
kubectl get pods 
```

---

## 🧹 Очистка базы данных

```bash
# 1. Удаление пода PostgreSQL
kubectl delete pod -l app=postgres

# 2. Под автоматически пересоздаётся с чистой базой данных
kubectl get pods
```

---

## 🏁 Завершение сессии

```bash
# 1. Проверка использования памяти Docker
docker system df

# 2. Очистка кэша Docker Builder
docker builder prune -a
```

---

> ⚠️ **Примечание:** Все команды выполнены для среды разработки с Minikube и Docker на Windows внутри CMD от АДМИНА.
