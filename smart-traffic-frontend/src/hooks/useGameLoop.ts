import { useEffect, useRef } from 'react';

/**
 * Hook customizado que executa uma função de "loop de jogo" (como requestAnimationFrame)
 * de forma consistente, passando o delta time (tempo desde o último frame) para
 * permitir animações baseadas em física e independentes de framerate.
 */
export const useGameLoop = (callback: (deltaTime: number) => void) => {
  // Guarda a referência da callback mais recente
  const callbackRef = useRef(callback);
  useEffect(() => {
    callbackRef.current = callback;
  }, [callback]);

  // Guarda a referência do ID da animação para poder cancelá-la
  const frameRef = useRef<number>();
  // Guarda o timestamp do último frame
  const lastTimeRef = useRef<number>();

  useEffect(() => {
    // A função que é chamada a cada frame
    const gameLoop = (time: number) => {
      if (lastTimeRef.current != null) {
        // Calcula o tempo que passou (em segundos)
        const deltaTime = (time - lastTimeRef.current) / 1000.0;
        // Chama a lógica de atualização do jogo
        callbackRef.current(deltaTime);
      }
      
      // Guarda o tempo atual para o próximo frame
      lastTimeRef.current = time;
      // Continua o loop
      frameRef.current = requestAnimationFrame(gameLoop);
    };

    // Inicia o loop
    frameRef.current = requestAnimationFrame(gameLoop);

    // Função de limpeza: cancela o loop quando o componente é desmontado
    return () => {
      if (frameRef.current) {
        cancelAnimationFrame(frameRef.current);
      }
    };
  }, []); // O array vazio garante que o loop inicie apenas uma vez
};

