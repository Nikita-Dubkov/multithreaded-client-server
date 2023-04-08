Многопоточное клиент серверное приложение на языке Java(version 17).

О структуре : 

  1)Application.java - класс выполняющий роль входной точки в приложение(Предоставляет выбор режима работы приложения).
  
  2)Client.java - класс выполняющий роль клиентского приложения.
  
  3)Server.java - класс выполняющий роль серверного приложенеия.
  
  4)Topic.java  - класс выполняющий роль раздела голосования
  
  5)Vote.java   - класс выполняющий роль голосования.
  
  6)log.properties - файл с настройками логирования
  
!!!Все логи пишутся в application_log.txt.i - максимальный размер файла 1000000 байт, при достижении лимита размера, будет создан новый файл с i+1, 
i = 0,1,2,3,4,5;!!!

!!!Server запускается на порту 8080!!!

!!!Серверные команды не работают без подключенных клиентов, после ввода команды, нужно получить любое сообщение от клиента, чтобы команды была выполнена!!!


Приложение поддерживает:

  ● Запуск в режиме клиента и режиме сервера
  
  ● Общение по протоколу TCP (или UDP) между сервером и клиентом
  
  ● В режиме сервера уметь принимать команды от нескольких клиентов одновременно
  
  ● Логирование в режиме сервера
  
    ● Клиентские команды
    
      ◦ login -u=username – подключиться к серверу с указанным именем
      пользователя (все остальные команды доступны только после выполнения
      login)
      ◦ create topic -n=<topic> - создает новый раздел c уникальным именем
       заданным в параметре -n
      ◦ view - показывает список уже созданных разделов в формате: <topic (votes in
      topic=<count>)>
        ▪ опциональный параметр -t=<topic> - в этом случае команда показывает
      список голосований в конкретном разделе
      ◦ create vote -t=<topic> - запускает создание нового голосования в разделе
      указанном в параметре -t
      Для создания голосования (команда create vote -t=<topic>) нужно
      последовательно запросить у пользователя:
        ● название (уникальное имя)
        ● тему голосования (описание)
        ● количество вариантов ответа
        ● варианты ответа
      ◦ view -t=<topic> -v=<vote> - отображает информацию по голосованию
        ▪ тема голосования
        ▪ варианты ответа и количество пользователей выбравших данный вариант
      ◦ vote -t=<topic> -v=<vote> - запускает выбор ответа в голосовании для текущего пользователя
        Для этого приложение
        ▪ приложение выводит варианты ответа для данного голосования
        ▪ запрашивает у пользователя выбор ответа
      ◦ delete -t=topic -v=<vote> - удалить голосование с именем <vote> из <topic>
        (удалить может только пользователь его создавший)
      ◦ exit - завершение работы программы

    ● Серверные команды
    ◦ load <filename> - загрузка данных из файла
    ◦ save <filename> – сохранение в файл всех созданных разделов и
      принадлежащим им голосований + их результатов
    ◦ exit - завершение работы программы
