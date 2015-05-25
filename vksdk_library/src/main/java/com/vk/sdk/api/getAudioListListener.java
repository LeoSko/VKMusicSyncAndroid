package com.vk.sdk.api;

public class getAudioListListener
{
    private final VKRequest.VKRequestListener listener;

    /**
     * @param listener listener for request events
     */
    public getAudioListListener(VKRequest.VKRequestListener listener)
    {
        this.listener = listener;
    }

    public VKRequest.VKRequestListener getListener()
    {
        return listener;
    }
}
