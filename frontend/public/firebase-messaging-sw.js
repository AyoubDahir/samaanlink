importScripts(
  'https://www.gstatic.com/firebasejs/11.0.0/firebase-app-compat.js'
);
importScripts(
  'https://www.gstatic.com/firebasejs/11.0.0/firebase-messaging-compat.js'
);

firebase.initializeApp({
  apiKey: 'AIzaSyDnsFyKQRA2foMl46wpS04NjxnFXOIhf5o',
  authDomain: 'bulshowallet.firebaseapp.com',
  projectId: 'bulshowallet',
  storageBucket: 'bulshowallet.firebasestorage.app',
  messagingSenderId: '747078570649',
  appId: '1:747078570649:web:86ff3d61fac51a3207de08'
});

const messaging = firebase.messaging();

messaging.onBackgroundMessage(function (payload) {
  const title =
    (payload && payload.notification && payload.notification.title) ||
    'Notification';
  const options = {
    body: payload && payload.notification && payload.notification.body,
    icon:
      (payload && payload.notification && payload.notification.icon) ||
      '/bulsho-logo.svg'
  };
  self.registration.showNotification(title, options);
});

self.addEventListener('notificationclick', function (event) {
  event.notification.close();
  event.waitUntil(
    clients
      .matchAll({ type: 'window', includeUncontrolled: true })
      .then((clientList) => {
        for (const client of clientList) {
          if ('focus' in client) return client.focus();
        }
        if (clients.openWindow) return clients.openWindow('/notifications');
      })
  );
});
