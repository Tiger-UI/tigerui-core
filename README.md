# TigerUI
Tools for creating reactive based UI applications

<a href='https://travis-ci.org/Tiger-UI/tigerui-core/builds'><img src='https://travis-ci.org/Tiger-UI/tigerui-core.svg?branch=master'></a>
[![Coverage Status](https://coveralls.io/repos/github/Tiger-UI/tigerui-core/badge.svg?branch=master)](https://coveralls.io/github/Tiger-UI/tigerui-core?branch=master)

### Motivation
#### Minimize Boilerplate
Creating UIs generally means wiring together components using listeners. Although writing listeners and callbacks is exciting in it's own way (heavy sigh!), writing all the boilerplate to achieve this is error prone and highly repetative. Never mind the ***callback hell*** that goes along with the traditional approach. In an ideal world we as developers should be focused on the look and feel, and behavior of the application, not it's plumbing. Using a reactive (streaming/event) based approach allows us to escape callback hell. In a nutshell, the library strives to minimize the amount of code you need to write in order to connect components, by removing the noise and boilerplate without loosing clarity.
#### Ensure thread correctness
Most UI toolkits are not thread safe, so if you're not careful you can introduce subtle bugs. Part of the goal of this library is to make it easier to ensure that we only interact with the underlying UI toolkit on it's event loop. This is achieved by guarding all access to state through a Property that will complain if accessed on the wrong thread. Also to be included is a general purpose abstraction for handling background tasks, something like the [ReactiveCommand](http://reactiveui.readthedocs.org/en/stable/basics/reactive-command/) as exists in the ReactiveUI library. A command will guarantee to never callback into a property on the wrong thread.
#### Improve Consistency and Re-Usability
In order to increase code re-use and agility there is a need for a minimal set of abstractions for the typical elements of a UI. Of course there are 5,001 different UI architectural patterns, this library will be loosely based on the [MVVM architecture](https://en.wikipedia.org/wiki/Model%E2%80%93view%E2%80%93viewmodel) with [Data Binding](https://en.wikipedia.org/wiki/Data_binding) at it's core. The MVVM pattern and data binding have been chosen since using this approach makes it easy to have a clean separation between the View and business logic of an application. An additional benefit of this architecture is that it is possible to have one team work on the View layer and another on the application, business logic layer. This means that the UI can be created by a UX designer using the appropriate tool and the application core by UI developers.
#### Portability
Since you may, at some time in the future, want to change the UI toolkit you are using this library attempts to limit the UI specific code to the View only. This may not be the case if the application you work on is a web app, but if you're lucky enough to be using Swing (half joking) this is definitely a concern. Therefore all interaction between the ViewModel and the View will be done using Properties and Data Binding. Since properties are technology agnostic they can be used regardless of the technology used to create the View. Clearly constructing and assembling the actual UI is toolkit specific, but the models, business logic and binding between the models and views should not be. The first targeted toolkit will be **Swing**, followed by **JavaFx** and possibly Html/Javascript (pipe dream).
#### Easy Client/Server Connectivity
Establishing a connection between your client application and the server should not involve space flight. It should be simple, flexible and quick to setup. Therefore the approach will be to use some event based communication, possibly **Reactive Microservices** or **Web Sockets**. The initial thought is to explore the following technologies [Vert.x](http://vertx.io/), [RatPack](https://ratpack.io/), [Reactor](http://projectreactor.io/), [Socket.IO](http://socket.io/), [Hazelcast](https://hazelcast.com/). I'm open to other technologies, so long as they're not blocking, RPC based. If you have a good idea convince me. Regardless of the technology that is used for the client/server communication, the library should be flexible to support whatever tool you like by extension. Another goal/desire would be to support bi-directional push/pull with the server.

### Inspiration
This library is heavily inspired by [ReactiveUI](http://reactiveui.net/), [RxJava](https://github.com/ReactiveX/RxJava), [sodium](https://github.com/SodiumFRP/sodium) and [Bacon.js](https://baconjs.github.io/). Although at the core the library does not use RxJava (*still on the fence about this*), the implementation of the Property API can be considered RxJava light specifically designed for UI development. Of course there is interop between RxJava and Properties.
#### Hold on a second, what, why aren't you just using RxJava
Well, as peviously mentioned, most UI toolkits are single threaded and you will have problems if you access the View from more than one thread. Considing this, it becomes clear that the implementation of the Property API must protect against wrong thread access. Attempting to bolt this protection on top of RxJava was not impossible but more of an annoyance. I did it and when I was done I was not happy with the result. Keep in mind also, the RxJava library was developed from the perspective of the server and although it can be used for UI, it inherits a lot of things not needed in the UI. The philosophy of this library is that all the code you write that interacts with the View should be in a happy single threaded bubble. Additionally, the API should be tuned for UI and not have to make any compromises. Of course you need concurrency and async but that probably represents a much smaller portion of the code than does the single threaded portion. Additionally RxJava is not the only way to achieve async, you could use actors, futures (sad face), promises / completable futures, etc... This library will not be married to one approach. You can use whatever form of concurrency you're comfortable with, so long as when you call into the Property API, it's on the right thread. While I'm at it, here's a note about using a `BehaviorSubjects` to back model properties.
##### What's wrong with the BehaviorSubject
There are four main problems with the `BehaviorSubject`

1. **Reentrancy** - ~~If~~ When you create binding loops, you can end up with reentrancy which ~~could~~ will cause problems. It should be noted that this is not a problem exclusive to the `BehaviorSubject`. I found a solution to this problem using the `BehaviorSubject`, but in the end I felt it could be easier.
2. **Unnecessary Error Events** - Since a property, be it in a Model or a View, replaces what would normally be a simple field with a getter and setter, why do you care about error events? You would never set an error on a property, so having to deal with that extra handler makes little sense. Of course a RuntimeException could be thrown if you attempt to set a value that is invalid, but that is a programmer error and not an expected possible outcome from calling a setter. These types of issues should be found during testing well before the code is shipped to a client. Therefore for UI code, you really only care about the `onNext` and `onCompleted` events.
3. **Early Completion** - Care must be taken when creating bindings between Subjects, since a completion or error in one Subject could complete the other if they are bound on `onError` and/or `onCompleted`. This could create a cascading effect which may inadvertently complete subjects that should not have been completed. The point is that in UI programming it is normal that elements have different lifetimes. Of course you could be careful to only subscribe `onNext` everywhere but why tip toe around like this if you don't have to.
4. **Value Retrieval** Considering a Property is meant to replace a mutable field in a Model, you should be able to get the value even after the property is no longer streaming values. A `BehaviorSubject` does not do this, it returns `null`. In contrast, an `AsyncSubject` only provides the value after completion, so in effect a Property acts like a combination of a `BehaviorSubject` and `AsyncSubject`.

### Where am I now
This library is in it's infancy. There is a lot of work to be done, so suggestions about design and implementation are welcome. The first thing I've worked on is the Property, Data Binding API. Since this is such a core element, I've spent considerable time developing it and it is thoroughly tested. It's not 100% nailed down (~~90~~70% maybe), so I'm open to suggestions. Here's an example that demonstrates how the Properties API can be used. I have yet to commit any type of model or view object, so please ignore that fact for the moment. The application presents a form with two text fields that are synchronized, so that edits in one field are mirrored in the other. Here's two examples, the first using Swing, the second using JavaFx. Note tht the only differences are the way in which the UI is constructed.
```java
public class SwingSynchronizedTextFieldApp {

    private static void buildAndShowSwingView() {
        TextComponent<JComponent> textComponent1 = new TextComponent<>(defaultSwingTextView(), "tacos");
        TextComponent<JComponent> textComponent2 = new TextComponent<>(defaultSwingTextView(), "");
        textComponent2.getModel().getTextProperty().synchronize(textComponent1.getModel().getTextProperty());
        
        initUI(textComponent1, textComponent2);
    }

    private static void initUI(TextComponent<JComponent> textComponent1, 
                               TextComponent<JComponent> textComponent2) {
        
        JFrame frame = new JFrame("Synchronized Text Field Test App");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel panel = new JPanel(new GridLayout(2, 1));
        
        panel.add(textComponent1.getView());
        panel.add(textComponent2.getView());
        
        frame.setContentPane(panel);
        
        frame.pack();
        frame.setVisible(true);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(SwingSynchronizedTextFieldApp::buildAndShowSwingView );
    }
}
```
JavaFx example
```java
public class JavaFxSynchronizedTextFieldApp extends Application {
    
    public JavaFxSynchronizedTextFieldApp() {}
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        TextComponent<Control> textComponent1 = new TextComponent<>(defaultJavaFxTextView(), "tacos");
        TextComponent<Control> textComponent2 = new TextComponent<>(defaultJavaFxTextView(), "");
        textComponent2.getModel().getTextProperty().synchronize(textComponent1.getModel().getTextProperty());
        
        initUI(textComponent1, textComponent2, primaryStage);
    }

    private static void initUI(TextComponent<Control> textComponent1, 
                               TextComponent<Control> textComponent2,
                               Stage primaryStage) {

        primaryStage.setTitle("Synchronized Text Field Test App");
        
        GridPane gridPane = new GridPane();
        gridPane.add(textComponent1.getView(), 1, 1);
        gridPane.add(textComponent2.getView(), 1, 2);
        primaryStage.setScene(new Scene(gridPane, 300, 250));
        primaryStage.show();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
```
