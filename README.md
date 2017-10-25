# Elevation tester
A playground for elevation on Android:

![Elevation tester demo](https://user-images.githubusercontent.com/153802/31993717-ff91b80c-b975-11e7-9d98-0ecfb34761e1.gif)

While you cannot tint an elevation shadow, you can tweak it in clever ways to obtain subtler shadows, 
or simply some specific effect. This can be seen in action in the [Squanchy](http://squanchy.net)
schedule screen, which uses a custom `OutlineProvider` to cast shadows for the cards that look more
like diffuse shadows (although they're very much still the area shadow that the Material guidelines
specify, there is no custom drawing code there).

<img alt="Squanchy schedule screen" src="https://user-images.githubusercontent.com/153802/31993901-8b958a2c-b976-11e7-833f-fad1ede7fb21.png" width=50% />

## Licence

See the `LICENSE` file. tl;dr it's Apache 2.0
