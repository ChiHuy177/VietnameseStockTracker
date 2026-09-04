import { useAppDispatch } from '@/app/hooks';
import type { PriceQuote } from '@/models/priceQuote';
import { Client, type IMessage } from '@stomp/stompjs';
import { useEffect } from 'react';
import { quotesReceived } from './priceBoardSlice';

function brokerUrl(): string {
  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
  return `${protocol}//${window.location.host}/ws`;
}

/** Subscribes to /topic/price-board for the lifetime of the app and pushes updates into Redux. */
export function useStompPriceBoard() {
  const dispatch = useAppDispatch();

  useEffect(() => {
    const client = new Client({
      brokerURL: brokerUrl(),
      reconnectDelay: 5000,
    });

    client.onConnect = () => {
      client.subscribe('/topic/price-board', (message: IMessage) => {
        const quotes = JSON.parse(message.body) as PriceQuote[];
        dispatch(quotesReceived(quotes));
      });
    };

    client.activate();

    return () => {
      void client.deactivate();
    };
  }, [dispatch]);
}
